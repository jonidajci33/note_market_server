package notes.seller.service.web;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import java.util.UUID;
import notes.seller.service.application.identity.AuthResult;
import notes.seller.service.application.identity.AuthService;
import notes.seller.service.domain.identity.Role;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.security.JwtProperties;
import notes.seller.service.security.SecurityConfig;
import notes.seller.service.security.UserDetailsServiceImpl;
import notes.seller.service.web.advice.GlobalExceptionHandler;
import notes.seller.service.web.identity.AuthController;
import notes.seller.service.web.identity.dto.LoginRequest;
import notes.seller.service.web.identity.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@EnableConfigurationProperties(JwtProperties.class)
class AuthControllerWebMvcTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockitoBean
	private AuthService authService;
	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

	@Test
	void register_shouldReturnToken() throws Exception {
		UserEntity user = new UserEntity();
		user.setId(UUID.randomUUID());
		user.setEmail("user@example.com");
		user.setRoles(Set.of(Role.CLIENT));
		when(authService.registerClient(anyString(), anyString()))
				.thenReturn(new AuthResult("token", user));
		RegisterRequest request = new RegisterRequest("user@example.com", "Password1!");

		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("token"));
	}

	@Test
	void login_shouldReturnToken() throws Exception {
		UserEntity user = new UserEntity();
		user.setId(UUID.randomUUID());
		user.setEmail("user@example.com");
		user.setRoles(Set.of(Role.CLIENT));
		when(authService.login(anyString(), anyString()))
				.thenReturn(new AuthResult("token", user));
		LoginRequest request = new LoginRequest("user@example.com", "Password1!");

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("token"));
	}
}
