package notes.seller.service.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import notes.seller.service.application.catalog.CatalogQueryService;
import notes.seller.service.application.catalog.CourseService;
import notes.seller.service.application.catalog.NoteService;
import notes.seller.service.domain.catalog.CourseStatus;
import notes.seller.service.domain.catalog.NoteStatus;
import notes.seller.service.persistence.catalog.CourseEntity;
import notes.seller.service.persistence.catalog.CategoryEntity;
import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.security.JwtProperties;
import notes.seller.service.security.SecurityConfig;
import notes.seller.service.security.UserDetailsServiceImpl;
import notes.seller.service.web.advice.GlobalExceptionHandler;
import notes.seller.service.web.catalog.CatalogController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CatalogController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@EnableConfigurationProperties(JwtProperties.class)
class CatalogControllerWebMvcTest {
	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private CatalogQueryService catalogQueryService;
	@MockitoBean
	private NoteService noteService;
	@MockitoBean
	private CourseService courseService;
	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

	private static NicheEntity nicheWithCategory() {
		CategoryEntity category = new CategoryEntity();
		category.setId(UUID.fromString("00000000-0000-0000-0000-00000000bb01"));
		category.setSlug("technology");
		category.setName("Technology");
		NicheEntity niche = new NicheEntity();
		niche.setId(UUID.randomUUID());
		niche.setCategory(category);
		return niche;
	}

	@Test
	void listNotes_shouldReturnPage() throws Exception {
		NoteEntity note = new NoteEntity();
		note.setId(UUID.randomUUID());
		note.setTitle("Note 1");
		note.setStatus(NoteStatus.PUBLISHED);
		note.setPrice(new BigDecimal("9.99"));
		note.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
		UserEntity seller = new UserEntity();
		seller.setId(UUID.randomUUID());
		note.setSeller(seller);
		note.setNiche(nicheWithCategory());
		when(catalogQueryService.findNotes(any(), any(), any(), any(), any(), any(), any()))
				.thenReturn(new PageImpl<>(List.of(note), PageRequest.of(0, 20), 1));

		mockMvc.perform(get("/api/v1/notes"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].title").value("Note 1"));
	}

	@Test
	void getCourse_shouldReturnCourse() throws Exception {
		CourseEntity course = new CourseEntity();
		course.setId(UUID.randomUUID());
		course.setTitle("Course 1");
		course.setStatus(CourseStatus.PUBLISHED);
		course.setPrice(new BigDecimal("19.99"));
		course.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
		UserEntity seller = new UserEntity();
		seller.setId(UUID.randomUUID());
		course.setSeller(seller);
		NicheEntity niche = new NicheEntity();
		niche.setId(UUID.randomUUID());
		course.setNiche(niche);
		when(courseService.getPublished(course.getId())).thenReturn(course);

		mockMvc.perform(get("/api/v1/courses/{id}", course.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Course 1"));
	}
}
