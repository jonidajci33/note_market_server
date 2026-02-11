package notes.seller.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import notes.seller.service.application.catalog.NoteService;
import notes.seller.service.application.catalog.UploadSession;
import notes.seller.service.domain.catalog.NoteStatus;
import notes.seller.service.integration.storage.PresignedUrl;
import notes.seller.service.integration.storage.StorageService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@Transactional
class NoteServiceIT extends AbstractPostgresIT {
	@Autowired
	private NoteService noteService;
	@Autowired
	private NoteRepository noteRepository;
	@Autowired
	private CourseRepository courseRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private NicheRepository nicheRepository;
	@MockitoBean
	private StorageService storageService;

	private TestDataFactory dataFactory;

	@BeforeEach
	void setUp() {
		dataFactory = TestDataFactory.seeded();
		when(storageService.createUploadUrl(any()))
				.thenReturn(new PresignedUrl("https://example.test/upload", Instant.parse("2024-01-01T00:00:00Z")));
	}

	@Test
	void create_shouldPersistNoteWithTags() {
		UserEntity seller = userRepository.save(dataFactory.aSellerUser());
		NicheEntity niche = nicheRepository.save(dataFactory.aNiche());

		NoteEntity note = noteService.create(seller.getId(), niche.getId(), null, "Title", "Desc",
				new BigDecimal("10.00"), Set.of("java", "spring"));

		assertThat(note.getId()).isNotNull();
		assertThat(note.getTags()).containsExactlyInAnyOrder("java", "spring");
	}

	@Test
	void create_shouldRejectCourseOwnedByAnotherSeller() {
		UserEntity seller = userRepository.save(dataFactory.aSellerUser());
		UserEntity otherSeller = userRepository.save(dataFactory.aSellerUser());
		NicheEntity niche = nicheRepository.save(dataFactory.aNiche());
		CourseEntity course = courseRepository.save(dataFactory.aCourse(otherSeller, niche));

		assertThatThrownBy(() -> noteService.create(seller.getId(), niche.getId(), course.getId(), "Title", null,
				new BigDecimal("10.00"), null))
				.isInstanceOf(ResponseStatusException.class)
				.satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
	}

	@Test
	void update_shouldChangeStatusAndTags() {
		UserEntity seller = userRepository.save(dataFactory.aSellerUser());
		NicheEntity niche = nicheRepository.save(dataFactory.aNiche());
		NoteEntity note = noteRepository.save(dataFactory.aNote(seller, niche));

		NoteEntity updated = noteService.update(seller.getId(), note.getId(), null, null, "New Title",
				"New Desc", new BigDecimal("20.00"), NoteStatus.PUBLISHED, Set.of("tag-a"));

		assertThat(updated.getStatus()).isEqualTo(NoteStatus.PUBLISHED);
		assertThat(updated.getTags()).containsExactly("tag-a");
	}

	@Test
	void requestUploadUrl_shouldStoreFileMetadata() {
		UserEntity seller = userRepository.save(dataFactory.aSellerUser());
		NicheEntity niche = nicheRepository.save(dataFactory.aNiche());
		NoteEntity note = noteRepository.save(dataFactory.aNote(seller, niche));

		UploadSession session = noteService.requestUploadUrl(seller.getId(), note.getId(), "application/pdf", 1024L, "sha256");

		assertThat(session.fileKey()).isNotBlank();
		NoteEntity refreshed = noteRepository.findById(note.getId()).orElseThrow();
		assertThat(refreshed.getFileKey()).isEqualTo(session.fileKey());
		assertThat(refreshed.getContentType()).isEqualTo("application/pdf");
		assertThat(refreshed.getFileSize()).isEqualTo(1024L);
		assertThat(refreshed.getChecksumSha256()).isEqualTo("sha256");
	}
}
