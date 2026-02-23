package notes.seller.service.web.rating.dto;

import java.time.Instant;
import java.util.UUID;

public record RatingResponse(
		UUID id,
		UUID userId,
		String displayName,
		int rating,
		String reviewText,
		Instant createdAt,
		Instant updatedAt
) {}
