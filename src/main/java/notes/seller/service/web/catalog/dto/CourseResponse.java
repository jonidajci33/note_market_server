package notes.seller.service.web.catalog.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import notes.seller.service.domain.catalog.CourseStatus;

public record CourseResponse(
		UUID id,
		UUID sellerId,
		UUID nicheId,
		String title,
		String description,
		BigDecimal price,
		CourseStatus status,
		Instant createdAt
) {
}