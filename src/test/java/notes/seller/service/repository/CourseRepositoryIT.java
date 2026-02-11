package notes.seller.service.repository;

import static notes.seller.service.support.DbAssertions.assertPersisted;
import static org.assertj.core.api.Assertions.assertThat;

import notes.seller.service.persistence.catalog.CourseEntity;
import notes.seller.service.persistence.catalog.CourseRepository;
import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.persistence.catalog.NicheRepository;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.persistence.identity.UserRepository;
import notes.seller.service.support.AbstractPostgresIT;
import notes.seller.service.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class CourseRepositoryIT extends AbstractPostgresIT {
	@Autowired
	private CourseRepository courseRepository;
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
	void save_shouldPersist_andFindByIdAndSellerId() {
		UserEntity seller = userRepository.save(dataFactory.aSellerUser());
		NicheEntity niche = nicheRepository.save(dataFactory.aNiche());
		CourseEntity course = dataFactory.aCourse(seller, niche);

		CourseEntity saved = courseRepository.save(course);

		assertPersisted(saved);
		UserEntity otherSeller = userRepository.save(dataFactory.aSellerUser());

		assertThat(courseRepository.findByIdAndSellerId(saved.getId(), seller.getId())).isPresent();
		assertThat(courseRepository.findByIdAndSellerId(saved.getId(), otherSeller.getId())).isEmpty();
	}
}
