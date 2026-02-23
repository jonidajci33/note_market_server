package notes.seller.service.web.rating;

import jakarta.validation.Valid;
import java.util.UUID;
import notes.seller.service.application.rating.RatingService;
import notes.seller.service.common.PageResponse;
import notes.seller.service.persistence.rating.RatingEntity;
import notes.seller.service.security.SecurityUtils;
import notes.seller.service.web.rating.dto.RatingRequest;
import notes.seller.service.web.rating.dto.RatingResponse;
import notes.seller.service.web.rating.dto.RatingSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notes/{noteId}/ratings")
public class RatingController {
	private final RatingService ratingService;

	public RatingController(RatingService ratingService) {
		this.ratingService = ratingService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public RatingResponse create(@PathVariable("noteId") UUID noteId,
								 @Valid @RequestBody RatingRequest request,
								 Authentication authentication) {
		UUID userId = SecurityUtils.getUserId(authentication);
		RatingEntity entity = ratingService.create(userId, noteId, request.rating(), request.reviewText());
		return toResponse(entity);
	}

	@PutMapping
	public RatingResponse update(@PathVariable("noteId") UUID noteId,
								 @Valid @RequestBody RatingRequest request,
								 Authentication authentication) {
		UUID userId = SecurityUtils.getUserId(authentication);
		RatingEntity entity = ratingService.update(userId, noteId, request.rating(), request.reviewText());
		return toResponse(entity);
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("noteId") UUID noteId, Authentication authentication) {
		UUID userId = SecurityUtils.getUserId(authentication);
		ratingService.delete(userId, noteId);
	}

	@GetMapping
	public PageResponse<RatingResponse> list(@PathVariable("noteId") UUID noteId,
											 @RequestParam(required = false, defaultValue = "0") int page,
											 @RequestParam(required = false, defaultValue = "20") int size) {
		int pageSize = Math.min(Math.max(size, 1), 100);
		Pageable pageable = PageRequest.of(Math.max(page, 0), pageSize);
		Page<RatingEntity> result = ratingService.listForNote(noteId, pageable);
		return PageResponse.from(result.map(this::toResponse));
	}

	@GetMapping("/summary")
	public RatingSummaryResponse summary(@PathVariable("noteId") UUID noteId) {
		RatingService.RatingSummary summary = ratingService.getSummary(noteId);
		return new RatingSummaryResponse(summary.averageRating(), summary.ratingCount());
	}

	@GetMapping("/mine")
	public RatingResponse mine(@PathVariable("noteId") UUID noteId, Authentication authentication) {
		UUID userId = SecurityUtils.getUserId(authentication);
		RatingEntity entity = ratingService.getMyRating(userId, noteId)
				.orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
						HttpStatus.NOT_FOUND, "You have not rated this note"));
		return toResponse(entity);
	}

	private RatingResponse toResponse(RatingEntity entity) {
		return new RatingResponse(
				entity.getId(),
				entity.getUser().getId(),
				ratingService.resolveDisplayName(entity.getUser()),
				entity.getRating(),
				entity.getReviewText(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}
}
