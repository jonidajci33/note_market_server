package notes.seller.service.web.commerce.dto;

import java.time.Instant;
import java.util.UUID;
import notes.seller.service.domain.commerce.ItemType;

public record LibraryItemResponse(
		UUID id,
		ItemType itemType,
		UUID itemId,
		Instant grantedAt
) {
}