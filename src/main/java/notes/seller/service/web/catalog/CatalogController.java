package notes.seller.service.web.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import notes.seller.service.application.catalog.CatalogQueryService;
import notes.seller.service.application.catalog.CourseService;
import notes.seller.service.application.catalog.NoteService;
import notes.seller.service.common.PageResponse;
import notes.seller.service.common.SortOption;
import notes.seller.service.persistence.catalog.CourseEntity;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.web.catalog.dto.CourseResponse;
import notes.seller.service.web.catalog.dto.CourseSummaryResponse;
import notes.seller.service.web.catalog.dto.NoteResponse;
import notes.seller.service.web.catalog.dto.NoteSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {
	private final CatalogQueryService catalogQueryService;
	private final NoteService noteService;
	private final CourseService courseService;

	public CatalogController(CatalogQueryService catalogQueryService, NoteService noteService, CourseService courseService) {
		this.catalogQueryService = catalogQueryService;
		this.noteService = noteService;
		this.courseService = courseService;
	}

	@GetMapping("/notes")
	public PageResponse<NoteSummaryResponse> listNotes(
			@RequestParam(required = false) UUID nicheId,
			@RequestParam(required = false) UUID sellerId,
			@RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) List<String> tags,
			@RequestParam(required = false, defaultValue = "CREATED_AT_DESC") String sort,
			@RequestParam(required = false, defaultValue = "0") int page,
			@RequestParam(required = false, defaultValue = "20") int size
	) {
		int pageSize = Math.min(Math.max(size, 1), 100);
		SortOption sortOption = SortOption.from(sort);
		Pageable pageable = PageRequest.of(Math.max(page, 0), pageSize, sortOption.toSort());
		Page<NoteEntity> result = catalogQueryService.findNotes(nicheId, sellerId, minPrice, maxPrice, q, tags, pageable);
		return PageResponse.from(result.map(this::toNoteSummary));
	}

	@GetMapping("/notes/{id}")
	public NoteResponse getNote(@PathVariable("id") UUID id) {
		NoteEntity note = noteService.getPublished(id);
		return toNoteResponse(note);
	}

	@GetMapping("/courses")
	public PageResponse<CourseSummaryResponse> listCourses(
			@RequestParam(required = false) UUID nicheId,
			@RequestParam(required = false) UUID sellerId,
			@RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice,
			@RequestParam(required = false) String q,
			@RequestParam(required = false, defaultValue = "CREATED_AT_DESC") String sort,
			@RequestParam(required = false, defaultValue = "0") int page,
			@RequestParam(required = false, defaultValue = "20") int size
	) {
		int pageSize = Math.min(Math.max(size, 1), 100);
		SortOption sortOption = SortOption.from(sort);
		Pageable pageable = PageRequest.of(Math.max(page, 0), pageSize, sortOption.toSort());
		Page<CourseEntity> result = catalogQueryService.findCourses(nicheId, sellerId, minPrice, maxPrice, q, pageable);
		return PageResponse.from(result.map(this::toCourseSummary));
	}

	@GetMapping("/courses/{id}")
	public CourseResponse getCourse(@PathVariable("id") UUID id) {
		CourseEntity course = courseService.getPublished(id);
		return toCourseResponse(course);
	}

	private NoteSummaryResponse toNoteSummary(NoteEntity note) {
		return new NoteSummaryResponse(
				note.getId(),
				note.getSeller().getId(),
				note.getNiche().getId(),
				note.getCourse() == null ? null : note.getCourse().getId(),
				note.getTitle(),
				noteService.resolveCoverImageUrl(note),
				note.getPrice(),
				note.getStatus(),
				note.getTags(),
				note.getCreatedAt()
		);
	}

	private NoteResponse toNoteResponse(NoteEntity note) {
		return new NoteResponse(
				note.getId(),
				note.getSeller().getId(),
				note.getNiche().getId(),
				note.getCourse() == null ? null : note.getCourse().getId(),
				note.getTitle(),
				note.getDescription(),
				noteService.resolveCoverImageUrl(note),
				note.getPrice(),
				note.getStatus(),
				note.getTags(),
				note.getCreatedAt()
		);
	}

	private CourseSummaryResponse toCourseSummary(CourseEntity course) {
		return new CourseSummaryResponse(
				course.getId(),
				course.getSeller().getId(),
				course.getNiche().getId(),
				course.getTitle(),
				course.getPrice(),
				course.getStatus(),
				course.getCreatedAt()
		);
	}

	private CourseResponse toCourseResponse(CourseEntity course) {
		return new CourseResponse(
				course.getId(),
				course.getSeller().getId(),
				course.getNiche().getId(),
				course.getTitle(),
				course.getDescription(),
				course.getPrice(),
				course.getStatus(),
				course.getCreatedAt()
		);
	}
}
