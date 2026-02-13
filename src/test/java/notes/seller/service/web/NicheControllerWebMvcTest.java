package notes.seller.service.web;

import static notes.seller.service.support.JwtTestUtils.jwtWithRole;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import notes.seller.service.application.catalog.NicheService;
import notes.seller.service.persistence.catalog.CategoryEntity;
import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.security.JwtProperties;
import notes.seller.service.security.SecurityConfig;
import notes.seller.service.security.UserDetailsServiceImpl;
import notes.seller.service.web.advice.GlobalExceptionHandler;
import notes.seller.service.web.catalog.NicheController;
import notes.seller.service.web.catalog.dto.NicheRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NicheController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@EnableConfigurationProperties(JwtProperties.class)
class NicheControllerWebMvcTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockitoBean
	private NicheService nicheService;
	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

	private static final UUID CATEGORY_ID = UUID.fromString("00000000-0000-0000-0000-00000000bb01");

	private NicheEntity nicheWithCategory(String slug, String name) {
		CategoryEntity category = new CategoryEntity();
		category.setId(CATEGORY_ID);
		category.setSlug("technology");
		category.setName("Technology");
		NicheEntity niche = new NicheEntity();
		niche.setId(UUID.randomUUID());
		niche.setSlug(slug);
		niche.setName(name);
		niche.setCategory(category);
		return niche;
	}

	@Test
	void list_shouldReturnNiches() throws Exception {
		NicheEntity niche = nicheWithCategory("ai", "AI");
		when(nicheService.listAll()).thenReturn(List.of(niche));

		mockMvc.perform(get("/api/v1/niches"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].slug").value("ai"));
	}

	@Test
	void create_shouldAllowSysadmin() throws Exception {
		NicheRequest request = new NicheRequest("java", "Java", CATEGORY_ID);
		NicheEntity niche = nicheWithCategory("java", "Java");
		when(nicheService.create(request.slug(), request.name(), request.categoryId())).thenReturn(niche);

		mockMvc.perform(post("/api/v1/niches")
						.with(jwtWithRole(UUID.randomUUID(), "SYSADMIN"))
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.slug").value("java"));
	}

	@Test
	void create_shouldRejectUnauthorized() throws Exception {
		NicheRequest request = new NicheRequest("java", "Java", null);

		mockMvc.perform(post("/api/v1/niches")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void create_shouldRejectWrongRole() throws Exception {
		NicheRequest request = new NicheRequest("java", "Java", CATEGORY_ID);

		mockMvc.perform(post("/api/v1/niches")
						.with(jwtWithRole(UUID.randomUUID(), "CLIENT"))
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden());
	}
}
