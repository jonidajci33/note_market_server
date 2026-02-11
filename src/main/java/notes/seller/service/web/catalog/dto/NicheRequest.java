package notes.seller.service.web.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record NicheRequest(
		@NotBlank String slug,
		@NotBlank String name,
		UUID parentId
) {
}