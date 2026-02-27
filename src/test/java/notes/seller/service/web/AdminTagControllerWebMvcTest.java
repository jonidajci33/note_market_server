package notes.seller.service.web;

import static notes.seller.service.support.JwtTestUtils.jwtWithRole;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import notes.seller.service.application.catalog.TagService;
import notes.seller.service.persistence.catalog.TagEntity;
import notes.seller.service.security.JwtProperties;
import notes.seller.service.security.SecurityConfig;
import notes.seller.service.security.UserDetailsServiceImpl;
import notes.seller.service.web.admin.AdminTagController;
import notes.seller.service.web.admin.dto.TagRequest;
import notes.seller.service.web.advice.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(AdminTagController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@EnableConfigurationProperties(JwtProperties.class)
class AdminTagControllerWebMvcTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
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
	void list_shouldReturnAllTags() throws Exception {
		UUID adminId = UUID.randomUUID();
		UUID tagId = UUID.randomUUID();
		when(tagService.listAll()).thenReturn(List.of(aTagEntity(tagId, "java", "Java")));

		mockMvc.perform(get("/api/v1/admin/tags")
						.with(jwtWithRole(adminId, "SYSADMIN")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].slug").value("java"))
				.andExpect(jsonPath("$[0].name").value("Java"));
	}

	@Test
	void list_shouldRejectNonAdmin() throws Exception {
		UUID userId = UUID.randomUUID();

		mockMvc.perform(get("/api/v1/admin/tags")
						.with(jwtWithRole(userId, "CLIENT")))
				.andExpect(status().isForbidden());
	}

	@Test
	void getById_shouldReturnTag() throws Exception {
		UUID adminId = UUID.randomUUID();
		UUID tagId = UUID.randomUUID();
		when(tagService.getById(eq(tagId))).thenReturn(aTagEntity(tagId, "spring", "Spring"));

		mockMvc.perform(get("/api/v1/admin/tags/{id}", tagId)
						.with(jwtWithRole(adminId, "SYSADMIN")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.slug").value("spring"))
				.andExpect(jsonPath("$.name").value("Spring"));
	}

	@Test
	void create_shouldReturn201() throws Exception {
		UUID adminId = UUID.randomUUID();
		UUID tagId = UUID.randomUUID();
		TagRequest request = new TagRequest("react", "React");
		when(tagService.create(eq("react"), eq("React"))).thenReturn(aTagEntity(tagId, "react", "React"));

		mockMvc.perform(post("/api/v1/admin/tags")
						.with(jwtWithRole(adminId, "SYSADMIN"))
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.slug").value("react"))
				.andExpect(jsonPath("$.name").value("React"));
	}

	@Test
	void create_shouldReturn409OnDuplicateSlug() throws Exception {
		UUID adminId = UUID.randomUUID();
		TagRequest request = new TagRequest("java", "Java");
		when(tagService.create(eq("java"), eq("Java")))
				.thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Tag slug already exists"));

		mockMvc.perform(post("/api/v1/admin/tags")
						.with(jwtWithRole(adminId, "SYSADMIN"))
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict());
	}

	@Test
	void update_shouldReturnUpdatedTag() throws Exception {
		UUID adminId = UUID.randomUUID();
		UUID tagId = UUID.randomUUID();
		TagRequest request = new TagRequest("kotlin", "Kotlin");
		when(tagService.update(eq(tagId), eq("kotlin"), eq("Kotlin")))
				.thenReturn(aTagEntity(tagId, "kotlin", "Kotlin"));

		mockMvc.perform(put("/api/v1/admin/tags/{id}", tagId)
						.with(jwtWithRole(adminId, "SYSADMIN"))
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.slug").value("kotlin"));
	}

	@Test
	void delete_shouldReturn204() throws Exception {
		UUID adminId = UUID.randomUUID();
		UUID tagId = UUID.randomUUID();
		doNothing().when(tagService).delete(eq(tagId));

		mockMvc.perform(delete("/api/v1/admin/tags/{id}", tagId)
						.with(jwtWithRole(adminId, "SYSADMIN")))
				.andExpect(status().isNoContent());
	}

	@Test
	void delete_shouldReturn409WhenTagInUse() throws Exception {
		UUID adminId = UUID.randomUUID();
		UUID tagId = UUID.randomUUID();
		doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete tag with associated notes"))
				.when(tagService).delete(eq(tagId));

		mockMvc.perform(delete("/api/v1/admin/tags/{id}", tagId)
						.with(jwtWithRole(adminId, "SYSADMIN")))
				.andExpect(status().isConflict());
	}

	@Test
	void addNicheAssociation_shouldReturn204() throws Exception {
		UUID adminId = UUID.randomUUID();
		UUID tagId = UUID.randomUUID();
		UUID nicheId = UUID.randomUUID();
		doNothing().when(tagService).addNicheAssociation(eq(tagId), eq(nicheId));

		mockMvc.perform(post("/api/v1/admin/tags/{id}/niches/{nicheId}", tagId, nicheId)
						.with(jwtWithRole(adminId, "SYSADMIN")))
				.andExpect(status().isNoContent());
	}

	@Test
	void removeNicheAssociation_shouldReturn204() throws Exception {
		UUID adminId = UUID.randomUUID();
		UUID tagId = UUID.randomUUID();
		UUID nicheId = UUID.randomUUID();
		doNothing().when(tagService).removeNicheAssociation(eq(tagId), eq(nicheId));

		mockMvc.perform(delete("/api/v1/admin/tags/{id}/niches/{nicheId}", tagId, nicheId)
						.with(jwtWithRole(adminId, "SYSADMIN")))
				.andExpect(status().isNoContent());
	}

	@Test
	void list_shouldRejectUnauthenticated() throws Exception {
		mockMvc.perform(get("/api/v1/admin/tags"))
				.andExpect(status().isUnauthorized());
	}
}
