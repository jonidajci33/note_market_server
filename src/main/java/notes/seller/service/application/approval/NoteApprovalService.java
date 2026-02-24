package notes.seller.service.application.approval;

import java.time.Instant;
import java.util.UUID;
import notes.seller.service.domain.catalog.NoteStatus;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.persistence.catalog.NoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class NoteApprovalService {
	private static final Logger log = LoggerFactory.getLogger(NoteApprovalService.class);
	static final int MAX_SUBMISSIONS = 4;

	private final NoteRepository noteRepository;

	public NoteApprovalService(NoteRepository noteRepository) {
		this.noteRepository = noteRepository;
	}

	public NoteEntity submitForApproval(UUID noteId, UUID sellerId) {
		NoteEntity note = noteRepository.findByIdAndSellerId(noteId, sellerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));

		if (note.getStatus() != NoteStatus.DRAFT && note.getStatus() != NoteStatus.REJECTED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Note can only be submitted from DRAFT or REJECTED status");
		}

		if (note.getFileKey() == null || note.getFileKey().isBlank()) {
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
					"Note file must be uploaded before submitting for approval");
		}

		if (note.getStatus() == NoteStatus.REJECTED) {
			if (note.getSubmissionCount() >= MAX_SUBMISSIONS) {
				throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
						"Maximum resubmission limit reached");
			}
			note.setSubmissionCount(note.getSubmissionCount() + 1);
		} else {
			// First submit from DRAFT
			note.setSubmissionCount(1);
		}

		note.setStatus(NoteStatus.PENDING_APPROVAL);
		note.setRejectionReason(null);
		note.setReviewedBy(null);
		note.setReviewedAt(null);

		log.info("Note submitted for approval: noteId={} sellerId={} submissionCount={}",
				noteId, sellerId, note.getSubmissionCount());
		return noteRepository.save(note);
	}

	public NoteEntity approve(UUID noteId, UUID adminUserId) {
		NoteEntity note = noteRepository.findById(noteId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));

		if (note.getStatus() != NoteStatus.PENDING_APPROVAL) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Only notes with PENDING_APPROVAL status can be approved");
		}

		note.setStatus(NoteStatus.PUBLISHED);
		note.setReviewedBy(adminUserId);
		note.setReviewedAt(Instant.now());

		log.info("Note approved: noteId={} adminUserId={}", noteId, adminUserId);
		return noteRepository.save(note);
	}

	public NoteEntity reject(UUID noteId, UUID adminUserId, String reason) {
		if (reason == null || reason.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Rejection reason is required");
		}

		NoteEntity note = noteRepository.findById(noteId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));

		if (note.getStatus() != NoteStatus.PENDING_APPROVAL) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Only notes with PENDING_APPROVAL status can be rejected");
		}

		note.setStatus(NoteStatus.REJECTED);
		note.setRejectionReason(reason);
		note.setReviewedBy(adminUserId);
		note.setReviewedAt(Instant.now());

		log.info("Note rejected: noteId={} adminUserId={} reason={}", noteId, adminUserId, reason);
		return noteRepository.save(note);
	}
}
