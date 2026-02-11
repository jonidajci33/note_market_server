package notes.seller.service.web.commerce.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import notes.seller.service.domain.commerce.ItemType;

public record OrderItemRequest(
		@NotNull ItemType itemType,
		@NotNull UUID itemId
) {
}