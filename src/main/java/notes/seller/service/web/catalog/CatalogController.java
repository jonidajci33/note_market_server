package notes.seller.service.web.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import notes.seller.service.application.catalog.CatalogQueryService;
import notes.seller.service.application.catalog.NoteService;
import notes.seller.service.common.PageResponse;
import notes.seller.service.common.SortOption;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.persistence.catalog.TagEntity;
import notes.seller.service.persistence.identity.SellerProfileEntity;
import notes.seller.service.web.catalog.dto.NoteDetailResponse;
import notes.seller.service.web.catalog.dto.NoteResponse;
import notes.seller.service.web.catalog.dto.NoteSummaryResponse;
import notes.seller.service.web.catalog.dto.TagInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Transactional(readOnly = true)
public class CatalogController {
	private final CatalogQueryService catalogQueryService;
	private final NoteService noteService;

	public CatalogController(CatalogQueryService catalogQueryService, NoteService noteService) {
		this.catalogQueryService = catalogQueryService;
		this.noteService = noteService;
	}

	@GetMapping("/notes")
	public PageResponse<NoteSummaryResponse> listNotes(
			@RequestParam(required = false) UUID nicheId,
			@RequestParam(required = false) UUID sellerId,
			@RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) List<UUID> tagIds,
			@RequestParam(required = false, defaultValue = "CREATED_AT_DESC") String sort,
			@RequestParam(required = false, defaultValue = "0") int page,
			@RequestParam(required = false, defaultValue = "20") int size
	) {
		int pageSize = Math.min(Math.max(size, 1), 100);
		SortOption sortOption = SortOption.from(sort);
		Pageable pageable = PageRequest.of(Math.max(page, 0), pageSize, sortOption.toSort());
		Page<NoteEntity> result = catalogQueryService.findNotes(nicheId, sellerId, minPrice, maxPrice, q, tagIds, pageable);
		return PageResponse.from(result.map(this::toNoteSummary));
	}

	@GetMapping("/notes/{id}")
	public NoteDetailResponse getNote(@PathVariable("id") UUID id) {
		NoteEntity note = noteService.getPublished(id);
		return toNoteDetailResponse(note);
	}

	private Set<TagInfo> toTagInfoSet(Set<TagEntity> tags) {
		if (tags == null) {
			return Set.of();
		}
		return tags.stream()
				.map(t -> new TagInfo(t.getId(), t.getName(), t.getSlug()))
				.collect(Collectors.toSet());
	}

	private NoteSummaryResponse toNoteSummary(NoteEntity note) {
		return new NoteSummaryResponse(
				note.getId(),
				note.getSeller().getId(),
				note.getNiche().getId(),
				note.getNiche().getCategory().getId(),
				note.getTitle(),
				noteService.resolveCoverImageUrl(note),
				note.getPrice(),
				note.getStatus(),
				toTagInfoSet(note.getTags()),
				note.getCreatedAt(),
				note.getAverageRating(),
				note.getRatingCount()
		);
	}

	private NoteResponse toNoteResponse(NoteEntity note) {
		int remaining = Math.max(0, 4 - note.getSubmissionCount());
		return new NoteResponse(
				note.getId(),
				note.getSeller().getId(),
				note.getNiche().getId(),
				note.getNiche().getCategory().getId(),
				note.getTitle(),
				note.getDescription(),
				noteService.resolveCoverImageUrl(note),
				note.getPrice(),
				note.getStatus(),
				toTagInfoSet(note.getTags()),
				note.getCreatedAt(),
				note.getRejectionReason(),
				note.getSubmissionCount(),
				remaining,
				note.getReviewedAt()
		);
	}

	private NoteDetailResponse toNoteDetailResponse(NoteEntity note) {
		SellerProfileEntity profile = note.getSeller().getSellerProfile();
		String displayName;
		if (profile != null && profile.getDisplayName() != null) {
			displayName = profile.getDisplayName();
		} else {
			String email = note.getSeller().getEmail();
			displayName = email != null && email.contains("@") ? email.substring(0, email.indexOf('@')) : "Unknown";
		}

		long sellerNoteCount = catalogQueryService.countPublishedNotesBySeller(note.getSeller().getId());

		NoteDetailResponse.SellerInfo sellerInfo = new NoteDetailResponse.SellerInfo(
				note.getSeller().getId(),
				displayName,
				sellerNoteCount
		);

		NoteDetailResponse.NicheInfo nicheInfo = new NoteDetailResponse.NicheInfo(
				note.getNiche().getId(),
				note.getNiche().getName(),
				note.getNiche().getSlug()
		);

		return new NoteDetailResponse(
				note.getId(),
				note.getSeller().getId(),
				note.getNiche().getId(),
				note.getNiche().getCategory().getId(),
				note.getTitle(),
				note.getDescription(),
				noteService.resolveCoverImageUrl(note),
				note.getPrice(),
				note.getStatus(),
				toTagInfoSet(note.getTags()),
				note.getCreatedAt(),
				note.getPages(),
				note.getContentType(),
				sellerInfo,
				nicheInfo,
				note.getAverageRating(),
				note.getRatingCount()
		);
	}

}
