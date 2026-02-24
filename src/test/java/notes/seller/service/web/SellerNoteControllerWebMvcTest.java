package notes.seller.service.web;

import static notes.seller.service.support.JwtTestUtils.jwtWithRole;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import notes.seller.service.application.approval.NoteApprovalService;
import notes.seller.service.application.catalog.NoteService;
import notes.seller.service.application.catalog.UploadSession;
import notes.seller.service.domain.catalog.NoteStatus;
import notes.seller.service.integration.storage.PresignedUrl;
import notes.seller.service.persistence.catalog.CategoryEntity;
import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.security.JwtProperties;
import notes.seller.service.security.SecurityConfig;
import notes.seller.service.security.UserDetailsServiceImpl;
import notes.seller.service.web.advice.GlobalExceptionHandler;
import notes.seller.service.web.catalog.SellerNoteController;
import notes.seller.service.web.catalog.dto.NoteCreateRequest;
import notes.seller.service.web.catalog.dto.NoteUploadRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

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
	private NoteApprovalService noteApprovalService;
	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

	private static NicheEntity nicheWithCategory(UUID nicheId) {
		CategoryEntity category = new CategoryEntity();
		category.setId(UUID.fromString("00000000-0000-0000-0000-00000000bb01"));
		category.setSlug("technology");
		category.setName("Technology");
		NicheEntity niche = new NicheEntity();
		niche.setId(nicheId);
		niche.setCategory(category);
		return niche;
	}

	private static NoteEntity aDraftNote(UUID sellerId) {
		NoteEntity note = new NoteEntity();
		note.setId(UUID.randomUUID());
		note.setTitle("Draft Note");
		note.setStatus(NoteStatus.DRAFT);
		note.setSubmissionCount(1);
		UserEntity seller = new UserEntity();
		seller.setId(sellerId);
		note.setSeller(seller);
		note.setNiche(nicheWithCategory(UUID.randomUUID()));
		return note;
	}

	@Test
	void listMyNotes_shouldReturnSellerNotes() throws Exception {
		UUID sellerId = UUID.randomUUID();
		NoteEntity note = aDraftNote(sellerId);
		when(noteService.listBySeller(eq(sellerId))).thenReturn(List.of(note));

		mockMvc.perform(get("/api/v1/seller/notes")
						.with(jwtWithRole(sellerId, "CLIENT")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("Draft Note"))
				.andExpect(jsonPath("$[0].status").value("DRAFT"))
				.andExpect(jsonPath("$[0].submissionCount").value(1))
				.andExpect(jsonPath("$[0].remainingSubmissions").value(3));
	}

	@Test
	void listMyNotes_shouldRejectUnauthorized() throws Exception {
		mockMvc.perform(get("/api/v1/seller/notes"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void create_shouldReturnDraftStatus() throws Exception {
		UUID sellerId = UUID.randomUUID();
		UUID nicheId = UUID.randomUUID();
		NoteCreateRequest request = new NoteCreateRequest(nicheId, null, "Title", "Desc", null, Set.of("tag"));
		NoteEntity note = new NoteEntity();
		note.setId(UUID.randomUUID());
		note.setTitle("Title");
		note.setStatus(NoteStatus.DRAFT);
		note.setSubmissionCount(1);
		UserEntity seller = new UserEntity();
		seller.setId(sellerId);
		note.setSeller(seller);
		note.setNiche(nicheWithCategory(nicheId));
		when(noteService.create(eq(sellerId), eq(nicheId), any(), eq("Title"), eq("Desc"), any(), any()))
				.thenReturn(note);

		mockMvc.perform(post("/api/v1/seller/notes")
						.with(jwtWithRole(sellerId, "SELLER"))
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("DRAFT"));
	}

	@Test
	void create_shouldAllowSeller() throws Exception {
		UUID sellerId = UUID.randomUUID();
		UUID nicheId = UUID.randomUUID();
		NoteCreateRequest request = new NoteCreateRequest(nicheId, null, "Title", "Desc", null, Set.of("tag"));
		NoteEntity note = new NoteEntity();
		note.setId(UUID.randomUUID());
		note.setTitle("Title");
		note.setStatus(NoteStatus.DRAFT);
		note.setSubmissionCount(1);
		UserEntity seller = new UserEntity();
		seller.setId(sellerId);
		note.setSeller(seller);
		note.setNiche(nicheWithCategory(nicheId));
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
	void create_shouldAllowAuthenticatedClientRole() throws Exception {
		UUID sellerId = UUID.randomUUID();
		UUID nicheId = UUID.randomUUID();
		NoteCreateRequest request = new NoteCreateRequest(nicheId, null, "Title", "Desc", null, Set.of("tag"));
		NoteEntity note = new NoteEntity();
		note.setId(UUID.randomUUID());
		note.setTitle("Title");
		note.setStatus(NoteStatus.DRAFT);
		note.setSubmissionCount(1);
		UserEntity seller = new UserEntity();
		seller.setId(sellerId);
		note.setSeller(seller);
		note.setNiche(nicheWithCategory(nicheId));
		when(noteService.create(eq(sellerId), eq(nicheId), any(), eq("Title"), eq("Desc"), any(), any()))
				.thenReturn(note);

		mockMvc.perform(post("/api/v1/seller/notes")
						.with(jwtWithRole(sellerId, "CLIENT"))
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Title"));
	}

	@Test
	void coverUpload_shouldReturnSession() throws Exception {
		UUID sellerId = UUID.randomUUID();
		UUID noteId = UUID.randomUUID();
		NoteUploadRequest request = new NoteUploadRequest("image/png", 1234L, null);
		when(noteService.requestCoverUploadUrl(eq(sellerId), eq(noteId), eq("image/png"), eq(1234L), eq((String) null)))
				.thenReturn(new UploadSession("notes/cover.png", new PresignedUrl("https://upload.test/cover", Instant.parse("2026-02-12T00:00:00Z"))));

		mockMvc.perform(post("/api/v1/seller/notes/{id}/cover-upload-url", noteId)
						.with(jwtWithRole(sellerId, "CLIENT"))
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fileKey").value("notes/cover.png"));
	}

	@Test
	void submit_shouldReturnPendingApproval() throws Exception {
		UUID sellerId = UUID.randomUUID();
		UUID noteId = UUID.randomUUID();
		NoteEntity note = new NoteEntity();
		note.setId(noteId);
		note.setTitle("Ready Note");
		note.setStatus(NoteStatus.PENDING_APPROVAL);
		note.setSubmissionCount(1);
		note.setFileKey("notes/test.pdf");
		UserEntity seller = new UserEntity();
		seller.setId(sellerId);
		note.setSeller(seller);
		note.setNiche(nicheWithCategory(UUID.randomUUID()));
		when(noteApprovalService.submitForApproval(eq(noteId), eq(sellerId))).thenReturn(note);

		mockMvc.perform(post("/api/v1/seller/notes/{id}/submit", noteId)
						.with(jwtWithRole(sellerId, "SELLER")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PENDING_APPROVAL"))
				.andExpect(jsonPath("$.submissionCount").value(1))
				.andExpect(jsonPath("$.remainingSubmissions").value(3));
	}

	@Test
	void submit_shouldReturn422WhenLimitReached() throws Exception {
		UUID sellerId = UUID.randomUUID();
		UUID noteId = UUID.randomUUID();
		when(noteApprovalService.submitForApproval(eq(noteId), eq(sellerId)))
				.thenThrow(new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Maximum resubmission limit reached"));

		mockMvc.perform(post("/api/v1/seller/notes/{id}/submit", noteId)
						.with(jwtWithRole(sellerId, "SELLER")))
				.andExpect(status().isUnprocessableEntity());
	}
}
