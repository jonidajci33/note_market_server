package notes.seller.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import notes.seller.service.application.catalog.CourseService;
import notes.seller.service.domain.catalog.CourseStatus;
import notes.seller.service.persistence.catalog.CourseEntity;
import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.persistence.catalog.NicheRepository;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.persistence.identity.UserRepository;
import notes.seller.service.support.AbstractPostgresIT;
import notes.seller.service.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@Transactional
class CourseServiceIT extends AbstractPostgresIT {
	@Autowired
	private CourseService courseService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private NicheRepository nicheRepository;

	private TestDataFactory dataFactory;

	@BeforeEach
	void setUp() {
		dataFactory = TestDataFactory.seeded();
	}

	@Test
	void create_shouldPersistDraftCourse() {
		UserEntity seller = userRepository.save(dataFactory.aSellerUser());
		NicheEntity niche = nicheRepository.save(dataFactory.aNiche());

		CourseEntity course = courseService.create(seller.getId(), niche.getId(), "Intro", "Desc", new BigDecimal("12.50"));

		assertThat(course.getId()).isNotNull();
		assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);
	}

	@Test
	void update_shouldModifyCourseFields() {
		UserEntity seller = userRepository.save(dataFactory.aSellerUser());
		NicheEntity niche = nicheRepository.save(dataFactory.aNiche());
		CourseEntity course = courseService.create(seller.getId(), niche.getId(), "Intro", "Desc", new BigDecimal("12.50"));

		CourseEntity updated = courseService.update(seller.getId(), course.getId(), "New Title", "New Desc", new BigDecimal("30.00"));

		assertThat(updated.getTitle()).isEqualTo("New Title");
		assertThat(updated.getDescription()).isEqualTo("New Desc");
		assertThat(updated.getPrice()).isEqualByComparingTo("30.00");
	}

	@Test
	void publish_shouldSetPublishedStatus() {
		UserEntity seller = userRepository.save(dataFactory.aSellerUser());
		NicheEntity niche = nicheRepository.save(dataFactory.aNiche());
		CourseEntity course = courseService.create(seller.getId(), niche.getId(), "Intro", "Desc", new BigDecimal("12.50"));

		CourseEntity published = courseService.publish(seller.getId(), course.getId());

		assertThat(published.getStatus()).isEqualTo(CourseStatus.PUBLISHED);
	}

	@Test
	void getPublished_shouldRejectDraftCourse() {
		UserEntity seller = userRepository.save(dataFactory.aSellerUser());
		NicheEntity niche = nicheRepository.save(dataFactory.aNiche());
		CourseEntity course = courseService.create(seller.getId(), niche.getId(), "Intro", "Desc", new BigDecimal("12.50"));

		assertThatThrownBy(() -> courseService.getPublished(course.getId()))
				.isInstanceOf(ResponseStatusException.class)
				.satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
	}
}
