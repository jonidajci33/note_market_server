package notes.seller.service.web.catalog.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import notes.seller.service.domain.catalog.NoteStatus;

public record NoteSummaryResponse(
		UUID id,
		UUID sellerId,
		UUID nicheId,
		UUID categoryId,
		String title,
		String coverImageUrl,
		BigDecimal price,
		NoteStatus status,
		Set<TagInfo> tags,
		Instant createdAt,
		Double averageRating,
		int ratingCount
) {
}
