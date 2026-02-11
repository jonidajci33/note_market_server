package notes.seller.service.application.catalog;

import java.math.BigDecimal;
import java.util.UUID;
import notes.seller.service.domain.catalog.CourseStatus;
import notes.seller.service.persistence.catalog.CourseEntity;
import notes.seller.service.persistence.catalog.CourseRepository;
import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.persistence.catalog.NicheRepository;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.persistence.identity.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CourseService {
	private final CourseRepository courseRepository;
	private final NicheRepository nicheRepository;
	private final UserRepository userRepository;

	public CourseService(CourseRepository courseRepository, NicheRepository nicheRepository, UserRepository userRepository) {
		this.courseRepository = courseRepository;
		this.nicheRepository = nicheRepository;
		this.userRepository = userRepository;
	}

	public CourseEntity create(UUID sellerId, UUID nicheId, String title, String description, BigDecimal price) {
		UserEntity seller = userRepository.findById(sellerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found"));
		NicheEntity niche = nicheRepository.findById(nicheId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Niche not found"));
		CourseEntity course = new CourseEntity();
		course.setSeller(seller);
		course.setNiche(niche);
		course.setTitle(title);
		course.setDescription(description);
		course.setPrice(price);
		course.setStatus(CourseStatus.DRAFT);
		return courseRepository.save(course);
	}

	public CourseEntity update(UUID sellerId, UUID courseId, String title, String description, BigDecimal price) {
		CourseEntity course = courseRepository.findByIdAndSellerId(courseId, sellerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
		if (title != null && !title.isBlank()) {
			course.setTitle(title);
		}
		if (description != null) {
			course.setDescription(description);
		}
		if (price != null) {
			course.setPrice(price);
		}
		return courseRepository.save(course);
	}

	public CourseEntity publish(UUID sellerId, UUID courseId) {
		CourseEntity course = courseRepository.findByIdAndSellerId(courseId, sellerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
		course.setStatus(CourseStatus.PUBLISHED);
		return courseRepository.save(course);
	}

	public CourseEntity getPublished(UUID courseId) {
		CourseEntity course = courseRepository.findById(courseId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
		if (course.getStatus() != CourseStatus.PUBLISHED) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
		}
		return course;
	}
}