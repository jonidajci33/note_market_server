package notes.seller.service.application.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import notes.seller.service.domain.catalog.CourseStatus;
import notes.seller.service.domain.catalog.NoteStatus;
import notes.seller.service.persistence.catalog.CourseEntity;
import notes.seller.service.persistence.catalog.CourseRepository;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.persistence.catalog.NoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CatalogQueryService {
	private final NoteRepository noteRepository;
	private final CourseRepository courseRepository;

	public CatalogQueryService(NoteRepository noteRepository, CourseRepository courseRepository) {
		this.noteRepository = noteRepository;
		this.courseRepository = courseRepository;
	}

	public Page<NoteEntity> findNotes(UUID nicheId, UUID sellerId, BigDecimal minPrice, BigDecimal maxPrice,
							 String query, List<String> tags, Pageable pageable) {
		Specification<NoteEntity> spec = Specification.where(NoteSpecifications.hasStatus(NoteStatus.PUBLISHED));
		spec = andIfPresent(spec, NoteSpecifications.hasNiche(nicheId));
		spec = andIfPresent(spec, NoteSpecifications.hasSeller(sellerId));
		spec = andIfPresent(spec, NoteSpecifications.priceBetween(minPrice, maxPrice));
		spec = andIfPresent(spec, NoteSpecifications.matchesQuery(query));
		spec = andIfPresent(spec, NoteSpecifications.hasTags(tags));
		return noteRepository.findAll(spec, pageable);
	}

	public Page<CourseEntity> findCourses(UUID nicheId, UUID sellerId, BigDecimal minPrice, BigDecimal maxPrice,
							   String query, Pageable pageable) {
		Specification<CourseEntity> spec = Specification.where(CourseSpecifications.hasStatus(CourseStatus.PUBLISHED));
		spec = andIfPresent(spec, CourseSpecifications.hasNiche(nicheId));
		spec = andIfPresent(spec, CourseSpecifications.hasSeller(sellerId));
		spec = andIfPresent(spec, CourseSpecifications.priceBetween(minPrice, maxPrice));
		spec = andIfPresent(spec, CourseSpecifications.matchesQuery(query));
		return courseRepository.findAll(spec, pageable);
	}

	public long countPublishedNotesBySeller(UUID sellerId) {
		return noteRepository.countBySellerIdAndStatus(sellerId, NoteStatus.PUBLISHED);
	}

	private static <T> Specification<T> andIfPresent(Specification<T> base, Specification<T> next) {
		if (next == null) {
			return base;
		}
		return base.and(next);
	}
}
