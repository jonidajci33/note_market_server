package notes.seller.service.web.catalog.dto;

import java.math.BigDecimal;

public record CourseUpdateRequest(
		String title,
		String description,
		BigDecimal price
) {
}