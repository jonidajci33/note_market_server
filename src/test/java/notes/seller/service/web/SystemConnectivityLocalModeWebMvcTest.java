package notes.seller.service.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import notes.seller.service.integration.storage.LocalFileStorageService;
import notes.seller.service.integration.storage.LocalStorageProperties;
import notes.seller.service.integration.storage.S3Properties;
import notes.seller.service.security.JwtProperties;
import notes.seller.service.security.SecurityConfig;
import notes.seller.service.security.UserDetailsServiceImpl;
import notes.seller.service.web.advice.GlobalExceptionHandler;
import notes.seller.service.web.system.SystemConnectivityController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.services.s3.S3Client;

@WebMvcTest(SystemConnectivityController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@EnableConfigurationProperties({JwtProperties.class, S3Properties.class, LocalStorageProperties.class})
@TestPropertySource(properties = {
		"app.storage.local.enabled=true",
		"app.storage.local.root-dir=./.storage-local-test",
		"app.storage.s3.enabled=false"
})
class SystemConnectivityLocalModeWebMvcTest {
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private S3Client s3Client;

	@MockitoBean
	private LocalFileStorageService localFileStorageService;

	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

	@Test
	void connectivity_shouldReportLocalStorageReachableWhenFilesystemIsAccessible() throws Exception {
		when(localFileStorageService.isStorageAccessible()).thenReturn(true);

		mockMvc.perform(get("/api/v1/system/connectivity"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.backendReachable").value(true))
				.andExpect(jsonPath("$.storageEnabled").value(true))
				.andExpect(jsonPath("$.minioReachable").value(true))
				.andExpect(jsonPath("$.bucket").value("local-filesystem"));
	}

	@Test
	void connectivity_shouldReportLocalStorageUnavailableWhenFilesystemIsNotAccessible() throws Exception {
		when(localFileStorageService.isStorageAccessible()).thenReturn(false);

		mockMvc.perform(get("/api/v1/system/connectivity"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.backendReachable").value(true))
				.andExpect(jsonPath("$.storageEnabled").value(true))
				.andExpect(jsonPath("$.minioReachable").value(false));
	}
}
