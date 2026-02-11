package notes.seller.service.web;

import static notes.seller.service.support.JwtTestUtils.jwtWithRole;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;
import notes.seller.service.application.delivery.DownloadService;
import notes.seller.service.integration.storage.PresignedUrl;
import notes.seller.service.security.JwtProperties;
import notes.seller.service.security.SecurityConfig;
import notes.seller.service.security.UserDetailsServiceImpl;
import notes.seller.service.web.advice.GlobalExceptionHandler;
import notes.seller.service.web.delivery.DownloadController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DownloadController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@EnableConfigurationProperties(JwtProperties.class)
class DownloadControllerWebMvcTest {
	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private DownloadService downloadService;
	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

	@Test
	void download_shouldReturnUrlForClient() throws Exception {
		UUID clientId = UUID.randomUUID();
		UUID noteId = UUID.randomUUID();
		when(downloadService.createDownloadUrl(clientId, noteId))
				.thenReturn(new PresignedUrl("https://example.test/download", Instant.parse("2024-01-01T00:00:00Z")));

		mockMvc.perform(post("/api/v1/notes/{id}/download", noteId)
						.with(jwtWithRole(clientId, "CLIENT")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.url").value("https://example.test/download"));
	}
}
