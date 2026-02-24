package notes.seller.service.web.admin;

import jakarta.validation.Valid;
import java.util.UUID;
import notes.seller.service.application.approval.NoteApprovalService;
import notes.seller.service.application.catalog.NoteService;
import notes.seller.service.domain.catalog.NoteStatus;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.persistence.catalog.NoteRepository;
import notes.seller.service.security.SecurityUtils;
import notes.seller.service.web.admin.dto.AdminNoteResponse;
import notes.seller.service.web.admin.dto.RejectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/admin/notes")
@PreAuthorize("hasRole('SYSADMIN')")
public class AdminNoteController {
	private static final Logger log = LoggerFactory.getLogger(AdminNoteController.class);
	private final NoteRepository noteRepository;
	private final NoteApprovalService noteApprovalService;
	private final NoteService noteService;

	public AdminNoteController(NoteRepository noteRepository,
							   NoteApprovalService noteApprovalService,
							   NoteService noteService) {
		this.noteRepository = noteRepository;
		this.noteApprovalService = noteApprovalService;
		this.noteService = noteService;
	}

	@GetMapping
	public Page<AdminNoteResponse> listNotes(
			@RequestParam(value = "status", defaultValue = "PENDING_APPROVAL") NoteStatus status,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "20") int size) {
		PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<NoteEntity> notes = noteRepository.findByStatus(status, pageable);
		return notes.map(this::toAdminResponse);
	}

	@GetMapping("/{id}")
	public AdminNoteResponse getNote(@PathVariable("id") UUID id) {
		NoteEntity note = noteRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
		return toAdminResponse(note);
	}

	@PostMapping("/{id}/approve")
	public AdminNoteResponse approve(@PathVariable("id") UUID id, Authentication authentication) {
		UUID adminId = SecurityUtils.getUserId(authentication);
		log.info("Admin approving note: noteId={} adminId={}", id, adminId);
		NoteEntity note = noteApprovalService.approve(id, adminId);
		return toAdminResponse(note);
	}

	@PostMapping("/{id}/reject")
	public AdminNoteResponse reject(@PathVariable("id") UUID id,
									@Valid @RequestBody RejectRequest request,
									Authentication authentication) {
		UUID adminId = SecurityUtils.getUserId(authentication);
		log.info("Admin rejecting note: noteId={} adminId={}", id, adminId);
		NoteEntity note = noteApprovalService.reject(id, adminId, request.reason());
		return toAdminResponse(note);
	}

	private AdminNoteResponse toAdminResponse(NoteEntity note) {
		int remaining = Math.max(0, 4 - note.getSubmissionCount());
		String sellerDisplayName = null;
		if (note.getSeller() != null && note.getSeller().getSellerProfile() != null) {
			sellerDisplayName = note.getSeller().getSellerProfile().getDisplayName();
		}
		if (sellerDisplayName == null && note.getSeller() != null) {
			sellerDisplayName = note.getSeller().getEmail();
		}

		return new AdminNoteResponse(
				note.getId(),
				note.getTitle(),
				note.getDescription(),
				noteService.resolveCoverImageUrl(note),
				note.getPrice(),
				note.getStatus(),
				note.getCreatedAt(),
				note.getSeller() != null ? note.getSeller().getId() : null,
				sellerDisplayName,
				note.getNiche() != null ? note.getNiche().getId() : null,
				note.getNiche() != null ? note.getNiche().getName() : null,
				note.getSubmissionCount(),
				remaining,
				note.getRejectionReason(),
				note.getReviewedBy(),
				note.getReviewedAt()
		);
	}
}
