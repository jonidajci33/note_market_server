package notes.seller.service.application.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import notes.seller.service.domain.catalog.NoteStatus;
import notes.seller.service.persistence.catalog.NoteEntity;
import org.springframework.data.jpa.domain.Specification;

public final class NoteSpecifications {
	private NoteSpecifications() {
	}

	public static Specification<NoteEntity> hasStatus(NoteStatus status) {
		if (status == null) {
			return null;
		}
		return (root, query, cb) -> cb.equal(root.get("status"), status);
	}

	public static Specification<NoteEntity> hasNiche(UUID nicheId) {
		if (nicheId == null) {
			return null;
		}
		return (root, query, cb) -> cb.equal(root.get("niche").get("id"), nicheId);
	}

	public static Specification<NoteEntity> hasSeller(UUID sellerId) {
		if (sellerId == null) {
			return null;
		}
		return (root, query, cb) -> cb.equal(root.get("seller").get("id"), sellerId);
	}

	public static Specification<NoteEntity> priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
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

	public static Specification<NoteEntity> matchesQuery(String queryText) {
		if (queryText == null || queryText.isBlank()) {
			return null;
		}
		String like = "%" + queryText.toLowerCase(Locale.ROOT) + "%";
		return (root, query, cb) -> cb.or(
				cb.like(cb.lower(root.get("title")), like),
				cb.like(cb.lower(root.get("description")), like)
		);
	}

	public static Specification<NoteEntity> hasTags(List<String> tags) {
		if (tags == null || tags.isEmpty()) {
			return null;
		}
		return (root, query, cb) -> {
			query.distinct(true);
			return root.join("tags").in(tags);
		};
	}
}