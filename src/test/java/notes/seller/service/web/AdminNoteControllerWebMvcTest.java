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
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import notes.seller.service.application.approval.NoteApprovalService;
import notes.seller.service.application.catalog.NoteService;
import notes.seller.service.domain.catalog.NoteStatus;
import notes.seller.service.persistence.catalog.CategoryEntity;
import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.persistence.catalog.NoteRepository;
import notes.seller.service.persistence.identity.SellerProfileEntity;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.security.JwtProperties;
import notes.seller.service.security.SecurityConfig;
import notes.seller.service.security.UserDetailsServiceImpl;
import notes.seller.service.web.admin.AdminNoteController;
import notes.seller.service.web.admin.dto.RejectRequest;
import notes.seller.service.web.advice.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@WebMvcTest(AdminNoteController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@EnableConfigurationProperties(JwtProperties.class)
class AdminNoteControllerWebMvcTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockitoBean
	private NoteRepository noteRepository;
	@MockitoBean
	private NoteApprovalService noteApprovalService;
	@MockitoBean
	private NoteService noteService;
	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

	private static NicheEntity nicheWithCategory() {
		CategoryEntity category = new CategoryEntity();
		category.setId(UUID.fromString("00000000-0000-0000-0000-00000000bb01"));
		category.setSlug("technology");
		category.setName("Technology");
		NicheEntity niche = new NicheEntity();
		niche.setId(UUID.randomUUID());
		niche.setName("Computer Science");
		niche.setCategory(category);
		return niche;
	}

	private static NoteEntity aPendingNote() {
		NoteEntity note = new NoteEntity();
		note.setId(UUID.randomUUID());
		note.setTitle("Pending Note");
		note.setDescription("A note awaiting approval");
		note.setStatus(NoteStatus.PENDING_APPROVAL);
		note.setPrice(new BigDecimal("9.99"));
		note.setSubmissionCount(1);
		note.setFileKey("notes/test.pdf");

		UserEntity seller = new UserEntity();
		seller.setId(UUID.randomUUID());
		seller.setEmail("seller@example.com");
		SellerProfileEntity profile = new SellerProfileEntity();
		profile.setDisplayName("Test Seller");
		profile.setUser(seller);
		seller.setSellerProfile(profile);
		note.setSeller(seller);
		note.setNiche(nicheWithCategory());
		return note;
	}

	@Test
	void listNotes_shouldRejectUnauthorized() throws Exception {
		mockMvc.perform(get("/api/v1/admin/notes"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void listNotes_shouldRejectNonSysadmin() throws Exception {
		UUID userId = UUID.randomUUID();
		mockMvc.perform(get("/api/v1/admin/notes")
						.with(jwtWithRole(userId, "SELLER")))
				.andExpect(status().isForbidden());
	}

	@Test
	void listNotes_shouldReturnPendingNotes() throws Exception {
		UUID adminId = UUID.randomUUID();
		NoteEntity note = aPendingNote();
		when(noteRepository.findByStatus(eq(NoteStatus.PENDING_APPROVAL), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(note), PageRequest.of(0, 20), 1));

		mockMvc.perform(get("/api/v1/admin/notes")
						.with(jwtWithRole(adminId, "SYSADMIN")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].title").value("Pending Note"))
				.andExpect(jsonPath("$.content[0].status").value("PENDING_APPROVAL"))
				.andExpect(jsonPath("$.content[0].sellerDisplayName").value("Test Seller"))
				.andExpect(jsonPath("$.content[0].submissionCount").value(1))
				.andExpect(jsonPath("$.content[0].remainingSubmissions").value(3));
	}

	@Test
	void getNote_shouldReturnNoteDetail() throws Exception {
		UUID adminId = UUID.randomUUID();
		NoteEntity note = aPendingNote();
		when(noteRepository.findById(note.getId())).thenReturn(Optional.of(note));

		mockMvc.perform(get("/api/v1/admin/notes/{id}", note.getId())
						.with(jwtWithRole(adminId, "SYSADMIN")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Pending Note"))
				.andExpect(jsonPath("$.sellerDisplayName").value("Test Seller"))
				.andExpect(jsonPath("$.nicheName").value("Computer Science"));
	}

	@Test
	void getNote_shouldReturn404ForMissing() throws Exception {
		UUID adminId = UUID.randomUUID();
		UUID noteId = UUID.randomUUID();
		when(noteRepository.findById(noteId)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/v1/admin/notes/{id}", noteId)
						.with(jwtWithRole(adminId, "SYSADMIN")))
				.andExpect(status().isNotFound());
	}

	@Test
	void approve_shouldReturnPublishedNote() throws Exception {
		UUID adminId = UUID.randomUUID();
		NoteEntity note = aPendingNote();
		note.setStatus(NoteStatus.PUBLISHED);
		note.setReviewedBy(adminId);
		note.setReviewedAt(Instant.now());
		when(noteApprovalService.approve(eq(note.getId()), eq(adminId))).thenReturn(note);

		mockMvc.perform(post("/api/v1/admin/notes/{id}/approve", note.getId())
						.with(jwtWithRole(adminId, "SYSADMIN")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PUBLISHED"))
				.andExpect(jsonPath("$.reviewedBy").value(adminId.toString()));
	}

	@Test
	void approve_shouldReturn409ForWrongStatus() throws Exception {
		UUID adminId = UUID.randomUUID();
		UUID noteId = UUID.randomUUID();
		when(noteApprovalService.approve(eq(noteId), eq(adminId)))
				.thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Only notes with PENDING_APPROVAL status can be approved"));

		mockMvc.perform(post("/api/v1/admin/notes/{id}/approve", noteId)
						.with(jwtWithRole(adminId, "SYSADMIN")))
				.andExpect(status().isConflict());
	}

	@Test
	void approve_shouldRejectNonSysadmin() throws Exception {
		UUID userId = UUID.randomUUID();
		UUID noteId = UUID.randomUUID();

		mockMvc.perform(post("/api/v1/admin/notes/{id}/approve", noteId)
						.with(jwtWithRole(userId, "CLIENT")))
				.andExpect(status().isForbidden());
	}

	@Test
	void reject_shouldReturnRejectedNote() throws Exception {
		UUID adminId = UUID.randomUUID();
		NoteEntity note = aPendingNote();
		note.setStatus(NoteStatus.REJECTED);
		note.setRejectionReason("Content violates guidelines");
		note.setReviewedBy(adminId);
		note.setReviewedAt(Instant.now());
		when(noteApprovalService.reject(eq(note.getId()), eq(adminId), eq("Content violates guidelines")))
				.thenReturn(note);

		RejectRequest request = new RejectRequest("Content violates guidelines");

		mockMvc.perform(post("/api/v1/admin/notes/{id}/reject", note.getId())
						.with(jwtWithRole(adminId, "SYSADMIN"))
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REJECTED"))
				.andExpect(jsonPath("$.rejectionReason").value("Content violates guidelines"));
	}

	@Test
	void reject_shouldReturn400ForBlankReason() throws Exception {
		UUID adminId = UUID.randomUUID();
		UUID noteId = UUID.randomUUID();

		RejectRequest request = new RejectRequest("");

		mockMvc.perform(post("/api/v1/admin/notes/{id}/reject", noteId)
						.with(jwtWithRole(adminId, "SYSADMIN"))
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void reject_shouldReturn409ForWrongStatus() throws Exception {
		UUID adminId = UUID.randomUUID();
		UUID noteId = UUID.randomUUID();
		when(noteApprovalService.reject(eq(noteId), eq(adminId), eq("Some reason")))
				.thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Only notes with PENDING_APPROVAL status can be rejected"));

		RejectRequest request = new RejectRequest("Some reason");

		mockMvc.perform(post("/api/v1/admin/notes/{id}/reject", noteId)
						.with(jwtWithRole(adminId, "SYSADMIN"))
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict());
	}

	@Test
	void reject_shouldRejectNonSysadmin() throws Exception {
		UUID userId = UUID.randomUUID();
		UUID noteId = UUID.randomUUID();

		RejectRequest request = new RejectRequest("Some reason");

		mockMvc.perform(post("/api/v1/admin/notes/{id}/reject", noteId)
						.with(jwtWithRole(userId, "SELLER"))
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden());
	}
}
