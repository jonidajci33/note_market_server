package notes.seller.service.repository;

import static notes.seller.service.support.DbAssertions.assertPersisted;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import notes.seller.service.domain.commerce.ItemType;
import notes.seller.service.persistence.commerce.OrderEntity;
import notes.seller.service.persistence.commerce.OrderRepository;
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
class OrderRepositoryIT extends AbstractPostgresIT {
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
	void save_shouldPersist_andFindByIdAndClientId() {
		UserEntity client = userRepository.save(dataFactory.aClientUser());
		OrderEntity order = dataFactory.anOrderWithSingleItem(client, ItemType.NOTE, UUID.randomUUID());

		OrderEntity saved = orderRepository.save(order);

		assertPersisted(saved);
		assertThat(orderRepository.findByIdAndClientId(saved.getId(), client.getId())).isPresent();
	}
}
