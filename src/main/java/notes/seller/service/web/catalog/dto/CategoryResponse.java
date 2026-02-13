package notes.seller.service.web.catalog.dto;

import java.util.UUID;

public record CategoryResponse(
	UUID id,
	String slug,
	String name
) {
}
