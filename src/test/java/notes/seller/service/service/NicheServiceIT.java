package notes.seller.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import notes.seller.service.application.catalog.NicheService;
import notes.seller.service.persistence.catalog.NicheEntity;
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
class NicheServiceIT extends AbstractPostgresIT {
	@Autowired
	private NicheService nicheService;

	private TestDataFactory dataFactory;

	@BeforeEach
	void setUp() {
		dataFactory = TestDataFactory.seeded();
	}

	@Test
	void create_shouldPersistNiche() {
		var request = dataFactory.aNicheRequest();

		NicheEntity niche = nicheService.create(request.slug(), request.name(), request.categoryId());

		assertThat(niche.getId()).isNotNull();
		assertThat(niche.getSlug()).isEqualTo(request.slug());
		assertThat(niche.getName()).isEqualTo(request.name());
	}

	@Test
	void create_shouldRejectDuplicateSlug() {
		var request = dataFactory.aNicheRequest();
		nicheService.create(request.slug(), request.name(), request.categoryId());

		assertThatThrownBy(() -> nicheService.create(request.slug(), "Other", null))
				.isInstanceOf(ResponseStatusException.class)
				.satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
	}
}
