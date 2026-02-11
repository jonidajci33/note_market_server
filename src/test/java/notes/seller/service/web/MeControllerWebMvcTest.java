package notes.seller.service.web;

import static notes.seller.service.support.JwtTestUtils.jwtWithRole;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.UUID;
import notes.seller.service.application.identity.UserService;
import notes.seller.service.domain.identity.Role;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.security.JwtProperties;
import notes.seller.service.security.SecurityConfig;
import notes.seller.service.security.UserDetailsServiceImpl;
import notes.seller.service.web.advice.GlobalExceptionHandler;
import notes.seller.service.web.identity.MeController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MeController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@EnableConfigurationProperties(JwtProperties.class)
class MeControllerWebMvcTest {
	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private UserService userService;
	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

	@Test
	void me_shouldReturnUserProfile() throws Exception {
		UUID userId = UUID.randomUUID();
		UserEntity user = new UserEntity();
		user.setId(userId);
		user.setEmail("user@example.com");
		user.setRoles(Set.of(Role.CLIENT));
		when(userService.getById(userId)).thenReturn(user);

		mockMvc.perform(get("/api/v1/me")
						.with(jwtWithRole(userId, "CLIENT")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("user@example.com"));
	}
}
