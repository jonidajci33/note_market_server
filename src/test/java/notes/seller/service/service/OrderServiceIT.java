package notes.seller.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import notes.seller.service.application.commerce.OrderItemCommand;
import notes.seller.service.application.commerce.OrderService;
import notes.seller.service.domain.catalog.NoteStatus;
import notes.seller.service.domain.commerce.ItemType;
import notes.seller.service.domain.commerce.OrderStatus;
import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.persistence.catalog.NicheRepository;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.persistence.catalog.NoteRepository;
import notes.seller.service.persistence.commerce.EntitlementRepository;
import notes.seller.service.persistence.commerce.OrderEntity;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.persistence.identity.UserRepository;
import notes.seller.service.support.AbstractPostgresIT;
import notes.seller.service.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@Transactional
class OrderServiceIT extends AbstractPostgresIT {
	@Autowired
	private OrderService orderService;
	@Autowired
	private NoteRepository noteRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private NicheRepository nicheRepository;
	@Autowired
	private EntitlementRepository entitlementRepository;

	private TestDataFactory dataFactory;

	@BeforeEach
	void setUp() {
		dataFactory = TestDataFactory.seeded();
	}

	@Test
	void createOrder_shouldCalculateTotal() {
		UserEntity client = userRepository.save(dataFactory.aClientUser());
		UserEntity seller = userRepository.save(dataFactory.aSellerUser());
		NicheEntity niche = nicheRepository.save(dataFactory.aNiche());
		NoteEntity note = dataFactory.aNote(seller, niche);
		note.setStatus(NoteStatus.PUBLISHED);
		note.setPrice(new BigDecimal("9.99"));
		noteRepository.save(note);

		OrderEntity order = orderService.createOrder(client.getId(),
				List.of(new OrderItemCommand(ItemType.NOTE, note.getId())));

		assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
		assertThat(order.getTotalAmount()).isEqualByComparingTo("9.99");
		assertThat(order.getItems()).hasSize(1);
	}

	@Test
	void payOrder_shouldSetPaidAndGrantEntitlements() {
		UserEntity client = userRepository.save(dataFactory.aClientUser());
		UserEntity seller = userRepository.save(dataFactory.aSellerUser());
		NicheEntity niche = nicheRepository.save(dataFactory.aNiche());
		NoteEntity note = dataFactory.aNote(seller, niche);
		note.setStatus(NoteStatus.PUBLISHED);
		note.setPrice(new BigDecimal("15.00"));
		noteRepository.save(note);

		OrderEntity order = orderService.createOrder(client.getId(),
				List.of(new OrderItemCommand(ItemType.NOTE, note.getId())));

		OrderEntity paid = orderService.payOrder(client.getId(), order.getId());

		assertThat(paid.getStatus()).isEqualTo(OrderStatus.PAID);
		assertThat(entitlementRepository.findByClientId(client.getId())).hasSize(1);
	}

	@Test
	void payOrder_shouldRejectWhenNotPending() {
		UserEntity client = userRepository.save(dataFactory.aClientUser());
		UserEntity seller = userRepository.save(dataFactory.aSellerUser());
		NicheEntity niche = nicheRepository.save(dataFactory.aNiche());
		NoteEntity note = dataFactory.aNote(seller, niche);
		note.setStatus(NoteStatus.PUBLISHED);
		note.setPrice(new BigDecimal("9.99"));
		noteRepository.save(note);

		OrderEntity order = orderService.createOrder(client.getId(),
				List.of(new OrderItemCommand(ItemType.NOTE, note.getId())));
		orderService.payOrder(client.getId(), order.getId());

		assertThatThrownBy(() -> orderService.payOrder(client.getId(), order.getId()))
				.isInstanceOf(ResponseStatusException.class)
				.satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
	}
}
