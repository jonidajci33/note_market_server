package notes.seller.service.application.commerce;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import notes.seller.service.domain.catalog.NoteStatus;
import notes.seller.service.domain.commerce.ItemType;
import notes.seller.service.domain.commerce.OrderStatus;
import notes.seller.service.domain.commerce.PaymentProvider;
import notes.seller.service.domain.commerce.PaymentStatus;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.persistence.catalog.NoteRepository;
import notes.seller.service.persistence.commerce.OrderEntity;
import notes.seller.service.persistence.commerce.OrderItemEntity;
import notes.seller.service.persistence.commerce.OrderRepository;
import notes.seller.service.persistence.commerce.PaymentEntity;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.persistence.identity.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class OrderService {
	private final OrderRepository orderRepository;
	private final NoteRepository noteRepository;
	private final UserRepository userRepository;
	private final EntitlementService entitlementService;

	public OrderService(OrderRepository orderRepository,
					NoteRepository noteRepository,
					UserRepository userRepository,
					EntitlementService entitlementService) {
		this.orderRepository = orderRepository;
		this.noteRepository = noteRepository;
		this.userRepository = userRepository;
		this.entitlementService = entitlementService;
	}

	public OrderEntity createOrder(UUID clientId, List<OrderItemCommand> items) {
		if (items == null || items.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order items required");
		}
		UserEntity client = userRepository.findById(clientId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));
		OrderEntity order = new OrderEntity();
		order.setClient(client);
		order.setStatus(OrderStatus.PENDING);
		order.setCurrency("USD");

		List<OrderItemEntity> orderItems = new ArrayList<>();
		BigDecimal total = BigDecimal.ZERO;
		for (OrderItemCommand item : items) {
			OrderItemEntity orderItem = new OrderItemEntity();
			orderItem.setOrder(order);
			orderItem.setItemType(item.itemType());
			orderItem.setItemId(item.itemId());
			BigDecimal unitPrice = resolvePrice(item);
			orderItem.setUnitPrice(unitPrice);
			orderItems.add(orderItem);
			total = total.add(unitPrice);
		}

		order.setItems(orderItems);
		order.setTotalAmount(total);
		return orderRepository.save(order);
	}

	public OrderEntity payOrder(UUID clientId, UUID orderId) {
		OrderEntity order = getOrder(clientId, orderId);
		if (order.getStatus() != OrderStatus.PENDING) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is not payable");
		}

		PaymentEntity payment = new PaymentEntity();
		payment.setOrder(order);
		payment.setProvider(PaymentProvider.STUB);
		payment.setStatus(PaymentStatus.PAID);
		payment.setProviderRef("stub-" + UUID.randomUUID());
		payment.setAmount(order.getTotalAmount());
		order.getPayments().add(payment);
		order.setStatus(OrderStatus.PAID);
		OrderEntity saved = orderRepository.save(order);

		grantEntitlements(saved);
		return saved;
	}

	public OrderEntity getOrder(UUID clientId, UUID orderId) {
		return orderRepository.findByIdAndClientId(orderId, clientId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
	}

	private void grantEntitlements(OrderEntity order) {
		UserEntity client = order.getClient();
		for (OrderItemEntity item : order.getItems()) {
			entitlementService.grantIfMissing(client, ItemType.NOTE, item.getItemId());
		}
	}

	private BigDecimal resolvePrice(OrderItemCommand item) {
		if (item.itemType() == ItemType.COURSE) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "COURSE item type is no longer supported");
		}
		if (item.itemType() == ItemType.NOTE) {
			NoteEntity note = noteRepository.findById(item.itemId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
			if (note.getStatus() != NoteStatus.PUBLISHED) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Note is not available");
			}
			if (note.getPrice() == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Note is not for individual sale");
			}
			return note.getPrice();
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported item type");
	}
}