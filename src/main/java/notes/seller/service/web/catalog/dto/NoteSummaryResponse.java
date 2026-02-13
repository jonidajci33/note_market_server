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
		UUID courseId,
		String title,
		String coverImageUrl,
		BigDecimal price,
		NoteStatus status,
		Set<String> tags,
		Instant createdAt
) {
}
