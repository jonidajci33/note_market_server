package notes.seller.service.web;

import static notes.seller.service.support.JwtTestUtils.jwtWithRole;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import notes.seller.service.application.commerce.EntitlementService;
import notes.seller.service.domain.commerce.ItemType;
import notes.seller.service.persistence.commerce.EntitlementEntity;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.security.JwtProperties;
import notes.seller.service.security.SecurityConfig;
import notes.seller.service.security.UserDetailsServiceImpl;
import notes.seller.service.web.advice.GlobalExceptionHandler;
import notes.seller.service.web.commerce.LibraryController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LibraryController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@EnableConfigurationProperties(JwtProperties.class)
class LibraryControllerWebMvcTest {
	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private EntitlementService entitlementService;
	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

	@Test
	void library_shouldReturnEntitlements() throws Exception {
		UUID clientId = UUID.randomUUID();
		EntitlementEntity entitlement = new EntitlementEntity();
		entitlement.setId(UUID.randomUUID());
		entitlement.setItemType(ItemType.NOTE);
		entitlement.setItemId(UUID.randomUUID());
		entitlement.setGrantedAt(Instant.parse("2024-01-01T00:00:00Z"));
		UserEntity client = new UserEntity();
		client.setId(clientId);
		entitlement.setClient(client);
		when(entitlementService.listForClient(clientId)).thenReturn(List.of(entitlement));

		mockMvc.perform(get("/api/v1/me/library")
						.with(jwtWithRole(clientId, "CLIENT")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));
	}
}
