package notes.seller.service.web;

import static notes.seller.service.support.JwtTestUtils.jwtWithRole;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import notes.seller.service.domain.commerce.ItemType;
import notes.seller.service.persistence.commerce.EntitlementEntity;
import notes.seller.service.persistence.commerce.EntitlementRepository;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.persistence.identity.UserRepository;
import notes.seller.service.support.AbstractPostgresIT;
import notes.seller.service.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LibraryControllerE2EIT extends AbstractPostgresIT {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private EntitlementRepository entitlementRepository;

	@Test
	void library_shouldReturnClientEntitlements() throws Exception {
		TestDataFactory dataFactory = TestDataFactory.seeded();
		UserEntity client = userRepository.save(dataFactory.aClientUser());
		EntitlementEntity entitlement = dataFactory.anEntitlement(client, ItemType.NOTE, UUID.randomUUID());
		entitlementRepository.save(entitlement);

		mockMvc.perform(get("/api/v1/me/library")
						.with(jwtWithRole(client.getId(), "CLIENT")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].itemType").value("NOTE"));
	}
}
