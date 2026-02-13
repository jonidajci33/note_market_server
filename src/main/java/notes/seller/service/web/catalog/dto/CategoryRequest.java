package notes.seller.service.web.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
	@NotBlank String slug,
	@NotBlank String name
) {
}
