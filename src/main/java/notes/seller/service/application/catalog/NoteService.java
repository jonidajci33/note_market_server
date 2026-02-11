package notes.seller.service.application.catalog;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import notes.seller.service.domain.catalog.NoteStatus;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class NoteService {
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
		note.setStatus(NoteStatus.PUBLISHED);
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
		if (note.getFileKey() == null || note.getFileKey().isBlank()) {
			String key = "notes/" + sellerId + "/" + noteId + "/" + UUID.randomUUID() + ".pdf";
			note.setFileKey(key);
		}
		note.setContentType(contentType);
		note.setFileSize(fileSize);
		note.setChecksumSha256(checksumSha256);
		noteRepository.save(note);
		PresignedUrl presignedUrl = storageService.createUploadUrl(new StorageUploadRequest(note.getFileKey(), contentType));
		return new UploadSession(note.getFileKey(), presignedUrl);
	}

	public NoteEntity getPublished(UUID noteId) {
		NoteEntity note = noteRepository.findById(noteId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
		if (note.getStatus() != NoteStatus.PUBLISHED) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found");
		}
		return note;
	}
}
