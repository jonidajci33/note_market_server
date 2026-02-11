package notes.seller.service.application.commerce;

import java.util.UUID;
import notes.seller.service.domain.commerce.ItemType;

public record OrderItemCommand(ItemType itemType, UUID itemId) {
}