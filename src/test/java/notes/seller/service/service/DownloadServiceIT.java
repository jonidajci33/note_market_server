package notes.seller.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;
import notes.seller.service.application.delivery.DownloadService;
import notes.seller.service.domain.commerce.ItemType;
import notes.seller.service.integration.storage.PresignedUrl;
import notes.seller.service.integration.storage.StorageService;
import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.persistence.catalog.NicheRepository;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.persistence.catalog.NoteRepository;
import notes.seller.service.persistence.commerce.EntitlementRepository;
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
class DownloadServiceIT extends AbstractPostgresIT {
	@Autowired
	private DownloadService downloadService;
	@Autowired
	private NoteRepository noteRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private NicheRepository nicheRepository;
	@Autowired
	private EntitlementRepository entitlementRepository;
	@MockitoBean
	private StorageService storageService;

	private TestDataFactory dataFactory;

	@BeforeEach
	void setUp() {
		dataFactory = TestDataFactory.seeded();
		when(storageService.createDownloadUrl(any()))
				.thenReturn(new PresignedUrl("https://example.test/download", Instant.parse("2024-01-01T00:00:00Z")));
	}

	@Test
	void createDownloadUrl_shouldReturnUrl_whenEntitled() {
		UserEntity client = userRepository.save(dataFactory.aClientUser());
		NicheEntity niche = nicheRepository.save(dataFactory.aNiche());
		NoteEntity note = noteRepository.save(dataFactory.aNote(userRepository.save(dataFactory.aSellerUser()), niche));
		note.setFileKey("notes/file.pdf");
		noteRepository.save(note);
		entitlementRepository.save(dataFactory.anEntitlement(client, ItemType.NOTE, note.getId()));

		PresignedUrl url = downloadService.createDownloadUrl(client.getId(), note.getId());

		assertThat(url.url()).isEqualTo("https://example.test/download");
	}

	@Test
	void createDownloadUrl_shouldRejectWhenNotEntitled() {
		UserEntity client = userRepository.save(dataFactory.aClientUser());
		NicheEntity niche = nicheRepository.save(dataFactory.aNiche());
		NoteEntity note = noteRepository.save(dataFactory.aNote(userRepository.save(dataFactory.aSellerUser()), niche));
		note.setFileKey("notes/file.pdf");
		noteRepository.save(note);

		assertThatThrownBy(() -> downloadService.createDownloadUrl(client.getId(), note.getId()))
				.isInstanceOf(ResponseStatusException.class)
				.satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
	}
}
