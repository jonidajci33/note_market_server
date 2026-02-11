package notes.seller.service.repository;

import static notes.seller.service.support.DbAssertions.assertPersisted;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import notes.seller.service.domain.commerce.ItemType;
import notes.seller.service.persistence.commerce.EntitlementEntity;
import notes.seller.service.persistence.commerce.EntitlementRepository;
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
class EntitlementRepositoryIT extends AbstractPostgresIT {
	@Autowired
	private EntitlementRepository entitlementRepository;
	@Autowired
	private UserRepository userRepository;

	private TestDataFactory dataFactory;

	@BeforeEach
	void setUp() {
		dataFactory = TestDataFactory.seeded();
	}

	@Test
	void existsByClientIdAndItemTypeAndItemId_shouldReturnTrue_whenExists() {
		UserEntity client = userRepository.save(dataFactory.aClientUser());
		UUID itemId = UUID.randomUUID();
		EntitlementEntity entitlement = dataFactory.anEntitlement(client, ItemType.NOTE, itemId);

		EntitlementEntity saved = entitlementRepository.save(entitlement);

		assertPersisted(saved);
		assertThat(entitlementRepository.existsByClientIdAndItemTypeAndItemId(client.getId(), ItemType.NOTE, itemId)).isTrue();
	}

	@Test
	void findByClientId_shouldReturnAllEntitlements() {
		UserEntity client = userRepository.save(dataFactory.aClientUser());
		entitlementRepository.save(dataFactory.anEntitlement(client, ItemType.NOTE, UUID.randomUUID()));
		entitlementRepository.save(dataFactory.anEntitlement(client, ItemType.COURSE, UUID.randomUUID()));

		List<EntitlementEntity> entitlements = entitlementRepository.findByClientId(client.getId());

		assertThat(entitlements).hasSize(2);
	}
}
