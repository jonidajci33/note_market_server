package notes.seller.service.web.commerce.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import notes.seller.service.domain.commerce.OrderStatus;

public record OrderResponse(
		UUID id,
		OrderStatus status,
		BigDecimal totalAmount,
		String currency,
		List<OrderItemResponse> items,
		Instant createdAt
) {
}