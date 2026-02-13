package notes.seller.service.web.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record NicheRequest(
		@NotBlank String slug,
		@NotBlank String name,
		@NotNull UUID categoryId
) {
}
