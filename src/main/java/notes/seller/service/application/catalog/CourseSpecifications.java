package notes.seller.service.application.catalog;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;
import notes.seller.service.domain.catalog.CourseStatus;
import notes.seller.service.persistence.catalog.CourseEntity;
import org.springframework.data.jpa.domain.Specification;

public final class CourseSpecifications {
	private CourseSpecifications() {
	}

	public static Specification<CourseEntity> hasStatus(CourseStatus status) {
		if (status == null) {
			return null;
		}
		return (root, query, cb) -> cb.equal(root.get("status"), status);
	}

	public static Specification<CourseEntity> hasNiche(UUID nicheId) {
		if (nicheId == null) {
			return null;
		}
		return (root, query, cb) -> cb.equal(root.get("niche").get("id"), nicheId);
	}

	public static Specification<CourseEntity> hasSeller(UUID sellerId) {
		if (sellerId == null) {
			return null;
		}
		return (root, query, cb) -> cb.equal(root.get("seller").get("id"), sellerId);
	}

	public static Specification<CourseEntity> priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
		if (minPrice == null && maxPrice == null) {
			return null;
		}
		return (root, query, cb) -> {
			if (minPrice != null && maxPrice != null) {
				return cb.between(root.get("price"), minPrice, maxPrice);
			}
			if (minPrice != null) {
				return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
			}
			return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
		};
	}

	public static Specification<CourseEntity> matchesQuery(String queryText) {
		if (queryText == null || queryText.isBlank()) {
			return null;
		}
		String like = "%" + queryText.toLowerCase(Locale.ROOT) + "%";
		return (root, query, cb) -> cb.or(
				cb.like(cb.lower(root.get("title")), like),
				cb.like(cb.lower(root.get("description")), like)
		);
	}
}