package notes.seller.service.application.rating;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import notes.seller.service.application.commerce.EntitlementService;
import notes.seller.service.domain.catalog.NoteStatus;
import notes.seller.service.domain.commerce.ItemType;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.persistence.catalog.NoteRepository;
import notes.seller.service.persistence.identity.SellerProfileEntity;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.persistence.identity.UserRepository;
import notes.seller.service.persistence.rating.RatingEntity;
import notes.seller.service.persistence.rating.RatingRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class RatingService {
	private final RatingRepository ratingRepository;
	private final NoteRepository noteRepository;
	private final UserRepository userRepository;
	private final EntitlementService entitlementService;

	public RatingService(RatingRepository ratingRepository, NoteRepository noteRepository,
						 UserRepository userRepository, EntitlementService entitlementService) {
		this.ratingRepository = ratingRepository;
		this.noteRepository = noteRepository;
		this.userRepository = userRepository;
		this.entitlementService = entitlementService;
	}

	public RatingEntity create(UUID userId, UUID noteId, int rating, String reviewText) {
		NoteEntity note = noteRepository.findById(noteId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
		if (note.getStatus() != NoteStatus.PUBLISHED) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found");
		}
		if (note.getSeller().getId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot rate your own note");
		}
		if (!entitlementService.hasEntitlement(userId, ItemType.NOTE, noteId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You must purchase this note before rating");
		}
		if (ratingRepository.existsByNoteIdAndUserId(noteId, userId)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already rated this note");
		}
		if (rating < 1 || rating > 5) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
		}

		UserEntity user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		RatingEntity entity = new RatingEntity();
		entity.setNote(note);
		entity.setUser(user);
		entity.setRating(rating);
		entity.setReviewText(reviewText);

		try {
			return ratingRepository.save(entity);
		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already rated this note");
		}
	}

	public RatingEntity update(UUID userId, UUID noteId, int rating, String reviewText) {
		RatingEntity entity = ratingRepository.findByNoteIdAndUserId(noteId, userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rating not found"));
		if (rating < 1 || rating > 5) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
		}
		entity.setRating(rating);
		entity.setReviewText(reviewText);
		return ratingRepository.save(entity);
	}

	public void delete(UUID userId, UUID noteId) {
		RatingEntity entity = ratingRepository.findByNoteIdAndUserId(noteId, userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rating not found"));
		ratingRepository.delete(entity);
	}

	@Transactional(readOnly = true)
	public Page<RatingEntity> listForNote(UUID noteId, Pageable pageable) {
		return ratingRepository.findByNoteIdOrderByCreatedAtDesc(noteId, pageable);
	}

	@Transactional(readOnly = true)
	public RatingSummary getSummary(UUID noteId) {
		Object[] row = ratingRepository.findSummaryByNoteId(noteId);
		if (row == null || row[0] == null) {
			return new RatingSummary(null, 0L);
		}
		Double avg = ((Number) row[0]).doubleValue();
		long count = ((Number) row[1]).longValue();
		return new RatingSummary(avg, count);
	}

	@Transactional(readOnly = true)
	public Map<UUID, RatingSummary> getSummaries(Collection<UUID> noteIds) {
		if (noteIds == null || noteIds.isEmpty()) {
			return Map.of();
		}
		List<Object[]> rows = ratingRepository.findSummariesByNoteIds(noteIds);
		Map<UUID, RatingSummary> map = new HashMap<>();
		for (Object[] row : rows) {
			UUID id = (UUID) row[0];
			Double avg = ((Number) row[1]).doubleValue();
			long count = ((Number) row[2]).longValue();
			map.put(id, new RatingSummary(avg, count));
		}
		return map;
	}

	@Transactional(readOnly = true)
	public Optional<RatingEntity> getMyRating(UUID userId, UUID noteId) {
		return ratingRepository.findByNoteIdAndUserId(noteId, userId);
	}

	public String resolveDisplayName(UserEntity user) {
		if (user == null) {
			return "Anonymous";
		}
		SellerProfileEntity profile = user.getSellerProfile();
		if (profile != null && profile.getDisplayName() != null && !profile.getDisplayName().isBlank()) {
			return profile.getDisplayName();
		}
		String email = user.getEmail();
		if (email != null && email.contains("@")) {
			return email.substring(0, email.indexOf('@'));
		}
		return "Anonymous";
	}

	public record RatingSummary(Double averageRating, long ratingCount) {}
}
