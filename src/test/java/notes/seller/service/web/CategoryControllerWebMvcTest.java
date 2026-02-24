package notes.seller.service.web;

import static notes.seller.service.support.JwtTestUtils.jwtWithRole;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import notes.seller.service.application.catalog.CategoryService;
import notes.seller.service.persistence.catalog.CategoryEntity;
import notes.seller.service.security.JwtProperties;
import notes.seller.service.security.SecurityConfig;
import notes.seller.service.security.UserDetailsServiceImpl;
import notes.seller.service.web.advice.GlobalExceptionHandler;
import notes.seller.service.web.catalog.CategoryController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(CategoryController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@EnableConfigurationProperties(JwtProperties.class)
class CategoryControllerWebMvcTest {
	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private CategoryService categoryService;
	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

	private CategoryEntity aCategory(String slug, String name) {
		CategoryEntity category = new CategoryEntity();
		category.setId(UUID.randomUUID());
		category.setSlug(slug);
		category.setName(name);
		return category;
	}

	// --- GET tests (baseline) ---

	@Test
	void list_shouldReturnCategories() throws Exception {
		CategoryEntity category = aCategory("tech", "Technology");
		when(categoryService.listAll()).thenReturn(List.of(category));

		mockMvc.perform(get("/api/v1/categories"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].slug").value("tech"));
	}

	// --- DELETE tests ---

	@Test
	void delete_shouldReturn204ForSysadmin() throws Exception {
		UUID categoryId = UUID.randomUUID();
		doNothing().when(categoryService).delete(categoryId);

		mockMvc.perform(delete("/api/v1/categories/{id}", categoryId)
						.with(jwtWithRole(UUID.randomUUID(), "SYSADMIN")))
				.andExpect(status().isNoContent());
	}

	@Test
	void delete_shouldReturn409WhenCategoryHasNiches() throws Exception {
		UUID categoryId = UUID.randomUUID();
		doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete category with associated niches"))
				.when(categoryService).delete(categoryId);

		mockMvc.perform(delete("/api/v1/categories/{id}", categoryId)
						.with(jwtWithRole(UUID.randomUUID(), "SYSADMIN")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Cannot delete category with associated niches"));
	}

	@Test
	void delete_shouldReturn404WhenCategoryNotFound() throws Exception {
		UUID categoryId = UUID.randomUUID();
		doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"))
				.when(categoryService).delete(categoryId);

		mockMvc.perform(delete("/api/v1/categories/{id}", categoryId)
						.with(jwtWithRole(UUID.randomUUID(), "SYSADMIN")))
				.andExpect(status().isNotFound());
	}

	@Test
	void delete_shouldReturn401WhenUnauthenticated() throws Exception {
		UUID categoryId = UUID.randomUUID();

		mockMvc.perform(delete("/api/v1/categories/{id}", categoryId))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void delete_shouldReturn403WhenNotSysadmin() throws Exception {
		UUID categoryId = UUID.randomUUID();

		mockMvc.perform(delete("/api/v1/categories/{id}", categoryId)
						.with(jwtWithRole(UUID.randomUUID(), "CLIENT")))
				.andExpect(status().isForbidden());
	}
}
