package notes.seller.service.web.catalog.dto;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import notes.seller.service.domain.catalog.NoteStatus;

public record NoteUpdateRequest(
		UUID nicheId,
		UUID courseId,
		String title,
		String description,
		BigDecimal price,
		NoteStatus status,
		Set<String> tags
) {
}