package notes.seller.service.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import notes.seller.service.application.catalog.TagService;
import notes.seller.service.persistence.catalog.TagEntity;
import notes.seller.service.security.JwtProperties;
import notes.seller.service.security.SecurityConfig;
import notes.seller.service.security.UserDetailsServiceImpl;
import notes.seller.service.web.advice.GlobalExceptionHandler;
import notes.seller.service.web.catalog.TagController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TagController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@EnableConfigurationProperties(JwtProperties.class)
class TagControllerWebMvcTest {
	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private TagService tagService;
	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

	private static TagEntity aTagEntity(UUID id, String slug, String name) {
		TagEntity tag = new TagEntity();
		tag.setId(id);
		tag.setSlug(slug);
		tag.setName(name);
		return tag;
	}

	@Test
	void list_shouldReturnAllTagsWithoutAuth() throws Exception {
		UUID tagId = UUID.randomUUID();
		when(tagService.listAll()).thenReturn(List.of(aTagEntity(tagId, "java", "Java")));

		mockMvc.perform(get("/api/v1/tags"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].slug").value("java"))
				.andExpect(jsonPath("$[0].name").value("Java"))
				.andExpect(jsonPath("$[0].id").value(tagId.toString()));
	}

	@Test
	void list_shouldFilterByNicheId() throws Exception {
		UUID nicheId = UUID.randomUUID();
		UUID tagId = UUID.randomUUID();
		when(tagService.listByNiche(eq(nicheId))).thenReturn(List.of(aTagEntity(tagId, "spring", "Spring")));

		mockMvc.perform(get("/api/v1/tags").param("nicheId", nicheId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].slug").value("spring"))
				.andExpect(jsonPath("$[0].name").value("Spring"));
	}

	@Test
	void list_shouldReturnEmptyListWhenNoTags() throws Exception {
		when(tagService.listAll()).thenReturn(List.of());

		mockMvc.perform(get("/api/v1/tags"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$").isEmpty());
	}
}
