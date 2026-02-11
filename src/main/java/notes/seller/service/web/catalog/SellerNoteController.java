package notes.seller.service.web.catalog;

import jakarta.validation.Valid;
import java.util.UUID;
import notes.seller.service.application.catalog.NoteService;
import notes.seller.service.application.catalog.UploadSession;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.security.SecurityUtils;
import notes.seller.service.web.catalog.dto.NoteCreateRequest;
import notes.seller.service.web.catalog.dto.NoteResponse;
import notes.seller.service.web.catalog.dto.NoteUpdateRequest;
import notes.seller.service.web.catalog.dto.NoteUploadRequest;
import notes.seller.service.web.catalog.dto.UploadUrlResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/seller/notes")
@PreAuthorize("hasRole('SELLER')")
public class SellerNoteController {
	private final NoteService noteService;

	public SellerNoteController(NoteService noteService) {
		this.noteService = noteService;
	}

	@PostMapping
	public NoteResponse create(@Valid @RequestBody NoteCreateRequest request, Authentication authentication) {
		UUID sellerId = SecurityUtils.getUserId(authentication);
		NoteEntity note = noteService.create(sellerId, request.nicheId(), request.courseId(), request.title(), request.description(),
				request.price(), request.tags());
		return toResponse(note);
	}

	@PutMapping("/{id}")
	public NoteResponse update(@PathVariable("id") UUID id,
							  @Valid @RequestBody NoteUpdateRequest request,
							  Authentication authentication) {
		UUID sellerId = SecurityUtils.getUserId(authentication);
		NoteEntity note = noteService.update(sellerId, id, request.nicheId(), request.courseId(), request.title(),
				request.description(), request.price(), request.status(), request.tags());
		return toResponse(note);
	}

	@PostMapping("/{id}/upload-url")
	public UploadUrlResponse uploadUrl(@PathVariable("id") UUID id,
							   @Valid @RequestBody NoteUploadRequest request,
							   Authentication authentication) {
		UUID sellerId = SecurityUtils.getUserId(authentication);
		UploadSession session = noteService.requestUploadUrl(sellerId, id, request.contentType(),
				request.fileSize(), request.checksumSha256());
		return new UploadUrlResponse(session.presignedUrl().url(), session.fileKey(), session.presignedUrl().expiresAt());
	}

	private NoteResponse toResponse(NoteEntity note) {
		return new NoteResponse(
				note.getId(),
				note.getSeller().getId(),
				note.getNiche().getId(),
				note.getCourse() == null ? null : note.getCourse().getId(),
				note.getTitle(),
				note.getDescription(),
				note.getPrice(),
				note.getStatus(),
				note.getTags(),
				note.getCreatedAt()
		);
	}
}