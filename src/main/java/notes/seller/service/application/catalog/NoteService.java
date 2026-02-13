package notes.seller.service.application.catalog;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import notes.seller.service.domain.catalog.NoteStatus;
import notes.seller.service.integration.storage.StorageDownloadRequest;
import notes.seller.service.integration.storage.PresignedUrl;
import notes.seller.service.integration.storage.StorageService;
import notes.seller.service.integration.storage.StorageUploadRequest;
import notes.seller.service.persistence.catalog.CourseEntity;
import notes.seller.service.persistence.catalog.CourseRepository;
import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.persistence.catalog.NicheRepository;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.persistence.catalog.NoteRepository;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.persistence.identity.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class NoteService {
	private static final Logger log = LoggerFactory.getLogger(NoteService.class);
	private final NoteRepository noteRepository;
	private final CourseRepository courseRepository;
	private final NicheRepository nicheRepository;
	private final UserRepository userRepository;
	private final StorageService storageService;

	public NoteService(NoteRepository noteRepository,
				  CourseRepository courseRepository,
				  NicheRepository nicheRepository,
				  UserRepository userRepository,
				  StorageService storageService) {
		this.noteRepository = noteRepository;
		this.courseRepository = courseRepository;
		this.nicheRepository = nicheRepository;
		this.userRepository = userRepository;
		this.storageService = storageService;
	}

	@Transactional(readOnly = true)
	public java.util.List<NoteEntity> listBySeller(UUID sellerId) {
		return noteRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
	}

	public NoteEntity create(UUID sellerId, UUID nicheId, UUID courseId, String title, String description,
						 BigDecimal price, Set<String> tags) {
		UserEntity seller = userRepository.findById(sellerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		NicheEntity niche = nicheRepository.findById(nicheId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Niche not found"));
		CourseEntity course = null; // course logic removed
		NoteEntity note = new NoteEntity();
		note.setSeller(seller);
		note.setNiche(niche);
		note.setCourse(course);
		note.setTitle(title);
		note.setDescription(description);
		note.setPrice(price);
		note.setStatus(NoteStatus.DRAFT);
		if (tags != null && !tags.isEmpty()) {
			note.setTags(new HashSet<>(tags));
		}
		return noteRepository.save(note);
	}

	public NoteEntity update(UUID sellerId, UUID noteId, UUID nicheId, UUID courseId, String title, String description,
						 BigDecimal price, NoteStatus status, Set<String> tags) {
		NoteEntity note = noteRepository.findByIdAndSellerId(noteId, sellerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
		if (nicheId != null) {
			NicheEntity niche = nicheRepository.findById(nicheId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Niche not found"));
			note.setNiche(niche);
		}
		if (courseId != null) {
			CourseEntity course = courseRepository.findById(courseId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
			if (!course.getSeller().getId().equals(sellerId)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Course does not belong to seller");
			}
			note.setCourse(course);
		}
		if (title != null && !title.isBlank()) {
			note.setTitle(title);
		}
		if (description != null) {
			note.setDescription(description);
		}
		if (price != null) {
			note.setPrice(price);
		}
		if (status != null) {
			note.setStatus(status);
		}
		if (tags != null) {
			note.setTags(new HashSet<>(tags));
		}
		return noteRepository.save(note);
	}

	public UploadSession requestUploadUrl(UUID sellerId, UUID noteId, String contentType, Long fileSize, String checksumSha256) {
		NoteEntity note = noteRepository.findByIdAndSellerId(noteId, sellerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
		boolean generatedNewKey = false;
		if (note.getFileKey() == null || note.getFileKey().isBlank()) {
			String key = "notes/" + sellerId + "/" + noteId + "/" + UUID.randomUUID() + ".pdf";
			note.setFileKey(key);
			generatedNewKey = true;
		}
		note.setContentType(contentType);
		note.setFileSize(fileSize);
		note.setChecksumSha256(checksumSha256);
		noteRepository.save(note);
		log.info("Preparing upload session: kind=NOTE_FILE sellerId={} noteId={} fileKey={} generatedNewKey={} contentType={} fileSize={}",
				sellerId, noteId, note.getFileKey(), generatedNewKey, contentType, fileSize);
		PresignedUrl presignedUrl = storageService.createUploadUrl(new StorageUploadRequest(note.getFileKey(), contentType));
		log.info("Upload session ready: kind=NOTE_FILE sellerId={} noteId={} fileKey={} expiresAt={}",
				sellerId, noteId, note.getFileKey(), presignedUrl.expiresAt());
		return new UploadSession(note.getFileKey(), presignedUrl);
	}

	public UploadSession requestCoverUploadUrl(UUID sellerId, UUID noteId, String contentType, Long fileSize, String checksumSha256) {
		NoteEntity note = noteRepository.findByIdAndSellerId(noteId, sellerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
		boolean generatedNewKey = false;
		if (note.getCoverFileKey() == null || note.getCoverFileKey().isBlank()) {
			String extension = extensionForContentType(contentType);
			String key = "notes/" + sellerId + "/" + noteId + "/cover-" + UUID.randomUUID() + "." + extension;
			note.setCoverFileKey(key);
			generatedNewKey = true;
		}
		note.setCoverContentType(contentType);
		noteRepository.save(note);
		log.info("Preparing upload session: kind=COVER_IMAGE sellerId={} noteId={} fileKey={} generatedNewKey={} contentType={} fileSize={}",
				sellerId, noteId, note.getCoverFileKey(), generatedNewKey, contentType, fileSize);
		PresignedUrl presignedUrl = storageService.createUploadUrl(new StorageUploadRequest(note.getCoverFileKey(), contentType));
		log.info("Upload session ready: kind=COVER_IMAGE sellerId={} noteId={} fileKey={} expiresAt={}",
				sellerId, noteId, note.getCoverFileKey(), presignedUrl.expiresAt());
		return new UploadSession(note.getCoverFileKey(), presignedUrl);
	}

	@Transactional(readOnly = true)
	public String resolveCoverImageUrl(NoteEntity note) {
		if (note.getCoverFileKey() == null || note.getCoverFileKey().isBlank()) {
			return null;
		}
		return storageService.createDownloadUrl(new StorageDownloadRequest(note.getCoverFileKey())).url();
	}

	public NoteEntity getPublished(UUID noteId) {
		NoteEntity note = noteRepository.findById(noteId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
		if (note.getStatus() != NoteStatus.PUBLISHED) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found");
		}
		return note;
	}

	private String extensionForContentType(String contentType) {
		if (contentType == null) {
			return "bin";
		}
		return switch (contentType.toLowerCase()) {
			case "image/jpeg", "image/jpg" -> "jpg";
			case "image/png" -> "png";
			case "image/webp" -> "webp";
			default -> "bin";
		};
	}
}
