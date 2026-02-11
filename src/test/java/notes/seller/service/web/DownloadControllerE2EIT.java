package notes.seller.service.web;

import static notes.seller.service.support.JwtTestUtils.jwtWithRole;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DownloadControllerE2EIT extends AbstractPostgresIT {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private NicheRepository nicheRepository;
	@Autowired
	private NoteRepository noteRepository;
	@Autowired
	private EntitlementRepository entitlementRepository;
	@MockitoBean
	private StorageService storageService;

	@Test
	void download_shouldReturnUrl() throws Exception {
		TestDataFactory dataFactory = TestDataFactory.seeded();
		UserEntity client = userRepository.save(dataFactory.aClientUser());
		UserEntity seller = userRepository.save(dataFactory.aSellerUser());
		NicheEntity niche = nicheRepository.save(dataFactory.aNiche());
		NoteEntity note = dataFactory.aNote(seller, niche);
		note.setFileKey("notes/file.pdf");
		noteRepository.save(note);
		entitlementRepository.save(dataFactory.anEntitlement(client, ItemType.NOTE, note.getId()));
		when(storageService.createDownloadUrl(any()))
				.thenReturn(new PresignedUrl("https://example.test/download", Instant.parse("2024-01-01T00:00:00Z")));

		mockMvc.perform(post("/api/v1/notes/{id}/download", note.getId())
						.with(jwtWithRole(client.getId(), "CLIENT")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.url").value("https://example.test/download"));
	}
}
