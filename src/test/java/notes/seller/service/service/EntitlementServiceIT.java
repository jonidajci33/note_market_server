package notes.seller.service.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import notes.seller.service.application.commerce.EntitlementService;
import notes.seller.service.domain.commerce.ItemType;
import notes.seller.service.persistence.commerce.EntitlementRepository;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.persistence.identity.UserRepository;
import notes.seller.service.support.AbstractPostgresIT;
import notes.seller.service.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class EntitlementServiceIT extends AbstractPostgresIT {
	@Autowired
	private EntitlementService entitlementService;
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
	void grantIfMissing_shouldCreateSingleEntitlement() {
		UserEntity client = userRepository.save(dataFactory.aClientUser());
		UUID itemId = UUID.randomUUID();

		entitlementService.grantIfMissing(client, ItemType.NOTE, itemId);
		entitlementService.grantIfMissing(client, ItemType.NOTE, itemId);

		assertThat(entitlementRepository.findByClientId(client.getId())).hasSize(1);
		assertThat(entitlementService.hasEntitlement(client.getId(), ItemType.NOTE, itemId)).isTrue();
	}
}
