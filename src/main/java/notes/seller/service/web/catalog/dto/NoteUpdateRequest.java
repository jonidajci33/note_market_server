package notes.seller.service.web.catalog.dto;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record NoteUpdateRequest(
		UUID nicheId,
		String title,
		String description,
		BigDecimal price,
		Set<UUID> tagIds
) {
}