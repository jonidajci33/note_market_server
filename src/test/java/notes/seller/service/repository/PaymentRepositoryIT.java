package notes.seller.service.repository;

import static notes.seller.service.support.DbAssertions.assertPersisted;
import static org.assertj.core.api.Assertions.assertThat;

import notes.seller.service.persistence.commerce.OrderEntity;
import notes.seller.service.persistence.commerce.OrderRepository;
import notes.seller.service.persistence.commerce.PaymentEntity;
import notes.seller.service.persistence.commerce.PaymentRepository;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.persistence.identity.UserRepository;
import notes.seller.service.support.AbstractPostgresIT;
import notes.seller.service.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PaymentRepositoryIT extends AbstractPostgresIT {
	@Autowired
	private PaymentRepository paymentRepository;
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private UserRepository userRepository;

	private TestDataFactory dataFactory;

	@BeforeEach
	void setUp() {
		dataFactory = TestDataFactory.seeded();
	}

	@Test
	void save_shouldPersist_andFindById() {
		UserEntity client = userRepository.save(dataFactory.aClientUser());
		OrderEntity order = orderRepository.save(dataFactory.anOrder(client));
		PaymentEntity payment = dataFactory.aPayment(order);

		PaymentEntity saved = paymentRepository.save(payment);

		assertPersisted(saved);
		assertThat(paymentRepository.findById(saved.getId())).isPresent();
	}
}
