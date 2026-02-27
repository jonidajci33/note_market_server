package notes.seller.service.web.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record NoteCreateRequest(
		@NotNull UUID nicheId,
		@NotBlank String title,
		String description,
		BigDecimal price,
		Set<UUID> tagIds
) {
}