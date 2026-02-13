package notes.seller.service.web.catalog.dto;

import java.util.UUID;

public record NicheResponse(
		UUID id,
		String slug,
		String name,
		UUID categoryId
) {
}
