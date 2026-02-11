package notes.seller.service.web.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CourseCreateRequest(
		@NotNull UUID nicheId,
		@NotBlank String title,
		String description,
		@NotNull BigDecimal price
) {
}