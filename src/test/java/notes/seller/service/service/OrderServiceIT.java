package notes.seller.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import notes.seller.service.application.commerce.OrderItemCommand;
import notes.seller.service.application.commerce.OrderService;
import notes.seller.service.domain.catalog.CourseStatus;
import notes.seller.service.domain.catalog.NoteStatus;
import notes.seller.service.domain.commerce.ItemType;
import notes.seller.service.domain.commerce.OrderStatus;
import notes.seller.service.persistence.catalog.CourseEntity;
import notes.seller.service.persistence.catalog.CourseRepository;
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
	private CourseRepository courseRepository;
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
		CourseEntity course = dataFactory.aCourse(seller, niche);
		course.setStatus(CourseStatus.PUBLISHED);
		course.setPrice(new BigDecimal("50.00"));
		courseRepository.save(course);
		NoteEntity note1 = dataFactory.aNote(seller, niche);
		note1.setCourse(course);
		noteRepository.save(note1);
		NoteEntity note2 = dataFactory.aNote(seller, niche);
		note2.setCourse(course);
		noteRepository.save(note2);

		OrderEntity order = orderService.createOrder(client.getId(),
				List.of(new OrderItemCommand(ItemType.COURSE, course.getId())));

		OrderEntity paid = orderService.payOrder(client.getId(), order.getId());

		assertThat(paid.getStatus()).isEqualTo(OrderStatus.PAID);
		assertThat(entitlementRepository.findByClientId(client.getId())).hasSize(3);
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
