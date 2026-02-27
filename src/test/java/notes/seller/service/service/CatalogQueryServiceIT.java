package notes.seller.service.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import notes.seller.service.application.catalog.CatalogQueryService;
import notes.seller.service.domain.catalog.NoteStatus;
import notes.seller.service.persistence.catalog.CategoryEntity;
import notes.seller.service.persistence.catalog.CategoryRepository;
import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.persistence.catalog.NicheRepository;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.persistence.catalog.NoteRepository;
import notes.seller.service.persistence.catalog.TagEntity;
import notes.seller.service.persistence.catalog.TagRepository;
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
	private UserRepository userRepository;
	@Autowired
	private NicheRepository nicheRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private TagRepository tagRepository;

	private TestDataFactory dataFactory;

	@BeforeEach
	void setUp() {
		dataFactory = TestDataFactory.seeded();
	}

	@Test
	void findNotes_shouldFilterByStatusAndTags() {
		UserEntity seller = userRepository.save(dataFactory.aSellerUser());
		CategoryEntity category = categoryRepository.save(dataFactory.aCategory());
		NicheEntity niche = nicheRepository.save(dataFactory.aNiche(category));
		TagEntity javaTag = tagRepository.save(dataFactory.aTag());
		TagEntity springTag = tagRepository.save(dataFactory.aTag());

		NoteEntity note1 = dataFactory.aNote(seller, niche);
		note1.setStatus(NoteStatus.PUBLISHED);
		note1.setPrice(new BigDecimal("10.00"));
		note1.setTags(Set.of(javaTag));
		noteRepository.save(note1);

		NoteEntity note2 = dataFactory.aNote(seller, niche);
		note2.setStatus(NoteStatus.PUBLISHED);
		note2.setTags(Set.of(springTag));
		noteRepository.save(note2);

		NoteEntity note3 = dataFactory.aNote(seller, niche);
		note3.setStatus(NoteStatus.DRAFT);
		note3.setTags(Set.of(javaTag));
		noteRepository.save(note3);

		var page = catalogQueryService.findNotes(niche.getId(), null, null, null, null,
				List.of(javaTag.getId()), PageRequest.of(0, 10));

		assertThat(page.getContent()).extracting(NoteEntity::getId).containsExactly(note1.getId());
	}

}
