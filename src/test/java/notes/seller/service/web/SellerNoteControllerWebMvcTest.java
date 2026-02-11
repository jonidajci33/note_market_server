package notes.seller.service.web;

import static notes.seller.service.support.JwtTestUtils.jwtWithRole;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import java.util.UUID;
import notes.seller.service.application.catalog.NoteService;
import notes.seller.service.domain.catalog.NoteStatus;
import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.security.JwtProperties;
import notes.seller.service.security.SecurityConfig;
import notes.seller.service.security.UserDetailsServiceImpl;
import notes.seller.service.web.advice.GlobalExceptionHandler;
import notes.seller.service.web.catalog.SellerNoteController;
import notes.seller.service.web.catalog.dto.NoteCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SellerNoteController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@EnableConfigurationProperties(JwtProperties.class)
class SellerNoteControllerWebMvcTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockitoBean
	private NoteService noteService;
	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

	@Test
	void create_shouldAllowSeller() throws Exception {
		UUID sellerId = UUID.randomUUID();
		UUID nicheId = UUID.randomUUID();
		NoteCreateRequest request = new NoteCreateRequest(nicheId, null, "Title", "Desc", null, Set.of("tag"));
		NoteEntity note = new NoteEntity();
		note.setId(UUID.randomUUID());
		note.setTitle("Title");
		note.setStatus(NoteStatus.DRAFT);
		UserEntity seller = new UserEntity();
		seller.setId(sellerId);
		note.setSeller(seller);
		NicheEntity niche = new NicheEntity();
		niche.setId(nicheId);
		note.setNiche(niche);
		when(noteService.create(eq(sellerId), eq(nicheId), any(), eq("Title"), eq("Desc"), any(), any()))
				.thenReturn(note);

		mockMvc.perform(post("/api/v1/seller/notes")
						.with(jwtWithRole(sellerId, "SELLER"))
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Title"));
	}

	@Test
	void create_shouldRejectUnauthorized() throws Exception {
		NoteCreateRequest request = new NoteCreateRequest(UUID.randomUUID(), null, "Title", "Desc", null, Set.of("tag"));

		mockMvc.perform(post("/api/v1/seller/notes")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void create_shouldRejectWrongRole() throws Exception {
		NoteCreateRequest request = new NoteCreateRequest(UUID.randomUUID(), null, "Title", "Desc", null, Set.of("tag"));

		mockMvc.perform(post("/api/v1/seller/notes")
						.with(jwtWithRole(UUID.randomUUID(), "CLIENT"))
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden());
	}
}
