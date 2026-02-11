package notes.seller.service.web.catalog.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import notes.seller.service.domain.catalog.CourseStatus;

public record CourseSummaryResponse(
		UUID id,
		UUID sellerId,
		UUID nicheId,
		String title,
		BigDecimal price,
		CourseStatus status,
		Instant createdAt
) {
}