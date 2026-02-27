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
import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.persistence.catalog.NicheRepository;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.persistence.catalog.NoteRepository;
import notes.seller.service.persistence.catalog.TagEntity;
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
	private final NicheRepository nicheRepository;
	private final UserRepository userRepository;
	private final StorageService storageService;
	private final TagService tagService;

	public NoteService(NoteRepository noteRepository,
				  NicheRepository nicheRepository,
				  UserRepository userRepository,
				  StorageService storageService,
				  TagService tagService) {
		this.noteRepository = noteRepository;
		this.nicheRepository = nicheRepository;
		this.userRepository = userRepository;
		this.storageService = storageService;
		this.tagService = tagService;
	}

	@Transactional(readOnly = true)
	public java.util.List<NoteEntity> listBySeller(UUID sellerId) {
		return noteRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
	}

	public NoteEntity create(UUID sellerId, UUID nicheId, String title, String description,
						 BigDecimal price, Set<UUID> tagIds) {
		UserEntity seller = userRepository.findById(sellerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		NicheEntity niche = nicheRepository.findById(nicheId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Niche not found"));
		NoteEntity note = new NoteEntity();
		note.setSeller(seller);
		note.setNiche(niche);
		note.setTitle(title);
		note.setDescription(description);
		note.setPrice(price);
		note.setStatus(NoteStatus.DRAFT);
		if (tagIds != null && !tagIds.isEmpty()) {
			Set<TagEntity> resolvedTags = tagService.resolveTagIds(tagIds);
			note.setTags(resolvedTags);
		}
		return noteRepository.save(note);
	}

	public NoteEntity update(UUID sellerId, UUID noteId, UUID nicheId, String title, String description,
						 BigDecimal price, Set<UUID> tagIds) {
		NoteEntity note = noteRepository.findByIdAndSellerId(noteId, sellerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));

		if (note.getStatus() != NoteStatus.DRAFT && note.getStatus() != NoteStatus.REJECTED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Note can only be edited in DRAFT or REJECTED status");
		}

		if (nicheId != null && !nicheId.equals(note.getNiche().getId())) {
			NicheEntity niche = nicheRepository.findById(nicheId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Niche not found"));
			note.setNiche(niche);
			note.getTags().clear();
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
		if (tagIds != null) {
			note.setTags(tagIds.isEmpty() ? new HashSet<>() : tagService.resolveTagIds(tagIds));
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
