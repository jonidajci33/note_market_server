package notes.seller.service.web.catalog;

import jakarta.validation.Valid;
import java.util.UUID;
import notes.seller.service.application.catalog.CourseService;
import notes.seller.service.persistence.catalog.CourseEntity;
import notes.seller.service.security.SecurityUtils;
import notes.seller.service.web.catalog.dto.CourseCreateRequest;
import notes.seller.service.web.catalog.dto.CourseResponse;
import notes.seller.service.web.catalog.dto.CourseUpdateRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/seller/courses")
public class SellerCourseController {
	private final CourseService courseService;

	public SellerCourseController(CourseService courseService) {
		this.courseService = courseService;
	}

	@PostMapping
	public CourseResponse create(@Valid @RequestBody CourseCreateRequest request, Authentication authentication) {
		UUID sellerId = SecurityUtils.getUserId(authentication);
		CourseEntity course = courseService.create(sellerId, request.nicheId(), request.title(), request.description(), request.price());
		return toResponse(course);
	}

	@PutMapping("/{id}")
	public CourseResponse update(@PathVariable("id") UUID id,
								 @Valid @RequestBody CourseUpdateRequest request,
								 Authentication authentication) {
		UUID sellerId = SecurityUtils.getUserId(authentication);
		CourseEntity course = courseService.update(sellerId, id, request.title(), request.description(), request.price());
		return toResponse(course);
	}

	@PostMapping("/{id}/publish")
	public CourseResponse publish(@PathVariable("id") UUID id, Authentication authentication) {
		UUID sellerId = SecurityUtils.getUserId(authentication);
		CourseEntity course = courseService.publish(sellerId, id);
		return toResponse(course);
	}

	private CourseResponse toResponse(CourseEntity course) {
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
