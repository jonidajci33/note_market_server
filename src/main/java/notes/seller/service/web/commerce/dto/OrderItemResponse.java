package notes.seller.service.web.commerce.dto;

import java.math.BigDecimal;
import java.util.UUID;
import notes.seller.service.domain.commerce.ItemType;

public record OrderItemResponse(
		ItemType itemType,
		UUID itemId,
		BigDecimal unitPrice
) {
}