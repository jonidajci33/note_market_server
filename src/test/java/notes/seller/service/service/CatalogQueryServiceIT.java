package notes.seller.service.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import notes.seller.service.application.catalog.CatalogQueryService;
import notes.seller.service.domain.catalog.CourseStatus;
import notes.seller.service.domain.catalog.NoteStatus;
import notes.seller.service.persistence.catalog.CourseEntity;
import notes.seller.service.persistence.catalog.CourseRepository;
import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.persistence.catalog.NicheRepository;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.persistence.catalog.NoteRepository;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.persistence.identity.UserRepository;
import notes.seller.service.support.AbstractPostgresIT;
import notes.seller.service.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class CatalogQueryServiceIT extends AbstractPostgresIT {
	@Autowired
	private CatalogQueryService catalogQueryService;
	@Autowired
	private NoteRepository noteRepository;
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
	void findNotes_shouldFilterByStatusAndTags() {
		UserEntity seller = userRepository.save(dataFactory.aSellerUser());
		NicheEntity niche = nicheRepository.save(dataFactory.aNiche());

		NoteEntity note1 = dataFactory.aNote(seller, niche);
		note1.setStatus(NoteStatus.PUBLISHED);
		note1.setPrice(new BigDecimal("10.00"));
		note1.setTags(Set.of("java"));
		noteRepository.save(note1);

		NoteEntity note2 = dataFactory.aNote(seller, niche);
		note2.setStatus(NoteStatus.PUBLISHED);
		note2.setTags(Set.of("spring"));
		noteRepository.save(note2);

		NoteEntity note3 = dataFactory.aNote(seller, niche);
		note3.setStatus(NoteStatus.DRAFT);
		note3.setTags(Set.of("java"));
		noteRepository.save(note3);

		var page = catalogQueryService.findNotes(niche.getId(), null, null, null, null,
				List.of("java"), PageRequest.of(0, 10));

		assertThat(page.getContent()).extracting(NoteEntity::getId).containsExactly(note1.getId());
	}

	@Test
	void findCourses_shouldReturnOnlyPublished() {
		UserEntity seller = userRepository.save(dataFactory.aSellerUser());
		NicheEntity niche = nicheRepository.save(dataFactory.aNiche());

		CourseEntity published = dataFactory.aCourse(seller, niche);
		published.setStatus(CourseStatus.PUBLISHED);
		published.setPrice(new BigDecimal("20.00"));
		courseRepository.save(published);

		CourseEntity draft = dataFactory.aCourse(seller, niche);
		draft.setStatus(CourseStatus.DRAFT);
		courseRepository.save(draft);

		var page = catalogQueryService.findCourses(niche.getId(), null, null, null, null, PageRequest.of(0, 10));

		assertThat(page.getContent()).extracting(CourseEntity::getId).containsExactly(published.getId());
	}
}
