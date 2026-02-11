package notes.seller.service.web.commerce;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import notes.seller.service.application.commerce.OrderItemCommand;
import notes.seller.service.application.commerce.OrderService;
import notes.seller.service.persistence.commerce.OrderEntity;
import notes.seller.service.persistence.commerce.OrderItemEntity;
import notes.seller.service.security.SecurityUtils;
import notes.seller.service.web.commerce.dto.OrderCreateRequest;
import notes.seller.service.web.commerce.dto.OrderItemResponse;
import notes.seller.service.web.commerce.dto.OrderResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@PreAuthorize("hasRole('CLIENT')")
public class OrderController {
	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	public OrderResponse create(@Valid @RequestBody OrderCreateRequest request, Authentication authentication) {
		UUID clientId = SecurityUtils.getUserId(authentication);
		List<OrderItemCommand> items = request.items().stream()
				.map(item -> new OrderItemCommand(item.itemType(), item.itemId()))
				.toList();
		OrderEntity order = orderService.createOrder(clientId, items);
		return toResponse(order);
	}

	@PostMapping("/{id}/pay")
	public OrderResponse pay(@PathVariable("id") UUID id, Authentication authentication) {
		UUID clientId = SecurityUtils.getUserId(authentication);
		OrderEntity order = orderService.payOrder(clientId, id);
		return toResponse(order);
	}

	@GetMapping("/{id}")
	public OrderResponse get(@PathVariable("id") UUID id, Authentication authentication) {
		UUID clientId = SecurityUtils.getUserId(authentication);
		OrderEntity order = orderService.getOrder(clientId, id);
		return toResponse(order);
	}

	private OrderResponse toResponse(OrderEntity order) {
		List<OrderItemResponse> items = order.getItems().stream().map(this::toItem).toList();
		return new OrderResponse(
				order.getId(),
				order.getStatus(),
				order.getTotalAmount(),
				order.getCurrency(),
				items,
				order.getCreatedAt()
		);
	}

	private OrderItemResponse toItem(OrderItemEntity item) {
		return new OrderItemResponse(item.getItemType(), item.getItemId(), item.getUnitPrice());
	}
}