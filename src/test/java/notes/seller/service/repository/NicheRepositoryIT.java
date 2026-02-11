package notes.seller.service.repository;

import static notes.seller.service.support.DbAssertions.assertPersisted;
import static org.assertj.core.api.Assertions.assertThat;

import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.persistence.catalog.NicheRepository;
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
class NicheRepositoryIT extends AbstractPostgresIT {
	@Autowired
	private NicheRepository nicheRepository;

	private TestDataFactory dataFactory;

	@BeforeEach
	void setUp() {
		dataFactory = TestDataFactory.seeded();
	}

	@Test
	void save_shouldPersist_andFindBySlug() {
		NicheEntity niche = dataFactory.aNiche();

		NicheEntity saved = nicheRepository.save(niche);

		assertPersisted(saved);
		assertThat(nicheRepository.findBySlug(saved.getSlug()))
				.isPresent()
				.get()
				.extracting(NicheEntity::getName)
				.isEqualTo(saved.getName());
	}

	@Test
	void findBySlug_shouldReturnEmpty_whenMissing() {
		assertThat(nicheRepository.findBySlug("missing")).isEmpty();
	}
}
