package notes.seller.service.web;

import static notes.seller.service.support.JwtTestUtils.jwtWithRole;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;
import notes.seller.service.application.catalog.CourseService;
import notes.seller.service.domain.catalog.CourseStatus;
import notes.seller.service.persistence.catalog.CourseEntity;
import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.security.JwtProperties;
import notes.seller.service.security.SecurityConfig;
import notes.seller.service.security.UserDetailsServiceImpl;
import notes.seller.service.web.advice.GlobalExceptionHandler;
import notes.seller.service.web.catalog.SellerCourseController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SellerCourseController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@EnableConfigurationProperties(JwtProperties.class)
class SellerCourseControllerWebMvcTest {
	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private CourseService courseService;
	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

	@Test
	void publish_shouldAllowSeller() throws Exception {
		UUID sellerId = UUID.randomUUID();
		UUID courseId = UUID.randomUUID();
		CourseEntity course = new CourseEntity();
		course.setId(courseId);
		course.setTitle("Course");
		course.setStatus(CourseStatus.PUBLISHED);
		course.setPrice(new BigDecimal("20.00"));
		UserEntity seller = new UserEntity();
		seller.setId(sellerId);
		course.setSeller(seller);
		NicheEntity niche = new NicheEntity();
		niche.setId(UUID.randomUUID());
		course.setNiche(niche);
		when(courseService.publish(eq(sellerId), eq(courseId))).thenReturn(course);

		mockMvc.perform(post("/api/v1/seller/courses/{id}/publish", courseId)
						.with(jwtWithRole(sellerId, "SELLER")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PUBLISHED"));
	}

	@Test
	void publish_shouldAllowAuthenticatedClientRole() throws Exception {
		UUID sellerId = UUID.randomUUID();
		UUID courseId = UUID.randomUUID();
		CourseEntity course = new CourseEntity();
		course.setId(courseId);
		course.setTitle("Course");
		course.setStatus(CourseStatus.PUBLISHED);
		course.setPrice(new BigDecimal("20.00"));
		UserEntity seller = new UserEntity();
		seller.setId(sellerId);
		course.setSeller(seller);
		NicheEntity niche = new NicheEntity();
		niche.setId(UUID.randomUUID());
		course.setNiche(niche);
		when(courseService.publish(eq(sellerId), eq(courseId))).thenReturn(course);

		mockMvc.perform(post("/api/v1/seller/courses/{id}/publish", courseId)
						.with(jwtWithRole(sellerId, "CLIENT")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PUBLISHED"));
	}
}
