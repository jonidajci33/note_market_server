package notes.seller.service.web.catalog;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import notes.seller.service.application.approval.NoteApprovalService;
import notes.seller.service.application.catalog.NoteService;
import notes.seller.service.application.catalog.UploadSession;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.security.SecurityUtils;
import notes.seller.service.web.catalog.dto.NoteCreateRequest;
import notes.seller.service.web.catalog.dto.NoteResponse;
import notes.seller.service.web.catalog.dto.NoteUpdateRequest;
import notes.seller.service.web.catalog.dto.NoteUploadRequest;
import notes.seller.service.web.catalog.dto.UploadUrlResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/seller/notes")
public class SellerNoteController {
	private static final Logger log = LoggerFactory.getLogger(SellerNoteController.class);
	private final NoteService noteService;
	private final NoteApprovalService noteApprovalService;

	public SellerNoteController(NoteService noteService, NoteApprovalService noteApprovalService) {
		this.noteService = noteService;
		this.noteApprovalService = noteApprovalService;
	}

	@GetMapping
	public List<NoteResponse> listMyNotes(Authentication authentication) {
		UUID sellerId = SecurityUtils.getUserId(authentication);
		return noteService.listBySeller(sellerId).stream().map(this::toResponse).toList();
	}

	@PostMapping
	public NoteResponse create(@Valid @RequestBody NoteCreateRequest request, Authentication authentication) {
		UUID sellerId = SecurityUtils.getUserId(authentication);
		log.info("Created Note: kind=NOTE sellerId={} ",
				sellerId);
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
				request.description(), request.price(), request.tags());
		return toResponse(note);
	}

	@PostMapping("/{id}/submit")
	public NoteResponse submitForApproval(@PathVariable("id") UUID id, Authentication authentication) {
		UUID sellerId = SecurityUtils.getUserId(authentication);
		log.info("Submit for approval: noteId={} sellerId={}", id, sellerId);
		NoteEntity note = noteApprovalService.submitForApproval(id, sellerId);
		return toResponse(note);
	}

	@PostMapping("/{id}/upload-url")
	public UploadUrlResponse uploadUrl(@PathVariable("id") UUID id,
							   @Valid @RequestBody NoteUploadRequest request,
							   Authentication authentication) {
		UUID sellerId = SecurityUtils.getUserId(authentication);
		log.info("Upload session requested: kind=NOTE_FILE sellerId={} noteId={} contentType={} fileSize={}",
				sellerId, id, request.contentType(), request.fileSize());
		UploadSession session = noteService.requestUploadUrl(sellerId, id, request.contentType(),
				request.fileSize(), request.checksumSha256());
		log.info("Upload session generated: kind=NOTE_FILE sellerId={} noteId={} fileKey={} expiresAt={}",
				sellerId, id, session.fileKey(), session.presignedUrl().expiresAt());
		return new UploadUrlResponse(session.presignedUrl().url(), session.fileKey(), session.presignedUrl().expiresAt());
	}

	@PostMapping("/{id}/cover-upload-url")
	public UploadUrlResponse coverUploadUrl(@PathVariable("id") UUID id,
									@Valid @RequestBody NoteUploadRequest request,
									Authentication authentication) {
		UUID sellerId = SecurityUtils.getUserId(authentication);
		log.info("Upload session requested: kind=COVER_IMAGE sellerId={} noteId={} contentType={} fileSize={}",
				sellerId, id, request.contentType(), request.fileSize());
		UploadSession session = noteService.requestCoverUploadUrl(sellerId, id, request.contentType(),
				request.fileSize(), request.checksumSha256());
		log.info("Upload session generated: kind=COVER_IMAGE sellerId={} noteId={} fileKey={} expiresAt={}",
				sellerId, id, session.fileKey(), session.presignedUrl().expiresAt());
		return new UploadUrlResponse(session.presignedUrl().url(), session.fileKey(), session.presignedUrl().expiresAt());
	}

	private NoteResponse toResponse(NoteEntity note) {
		int remaining = Math.max(0, 4 - note.getSubmissionCount());
		return new NoteResponse(
				note.getId(),
				note.getSeller().getId(),
				note.getNiche().getId(),
				note.getNiche().getCategory().getId(),
				note.getCourse() == null ? null : note.getCourse().getId(),
				note.getTitle(),
				note.getDescription(),
				noteService.resolveCoverImageUrl(note),
				note.getPrice(),
				note.getStatus(),
				note.getTags(),
				note.getCreatedAt(),
				note.getRejectionReason(),
				note.getSubmissionCount(),
				remaining,
				note.getReviewedAt()
		);
	}
}
