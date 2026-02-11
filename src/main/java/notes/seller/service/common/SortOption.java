package notes.seller.service.common;

import java.util.Locale;
import org.springframework.data.domain.Sort;

public enum SortOption {
	CREATED_AT_DESC,
	PRICE_ASC,
	PRICE_DESC;

	public static SortOption from(String value) {
		if (value == null || value.isBlank()) {
			return CREATED_AT_DESC;
		}
		String normalized = value.trim().toUpperCase(Locale.ROOT)
				.replace("-", "_")
				.replace(" ", "_");
		if (normalized.equals("PRICEASC")) {
			normalized = "PRICE_ASC";
		}
		if (normalized.equals("PRICEDESC")) {
			normalized = "PRICE_DESC";
		}
		if (normalized.equals("CREATEDATDESC")) {
			normalized = "CREATED_AT_DESC";
		}
		return switch (normalized) {
			case "PRICE_ASC" -> PRICE_ASC;
			case "PRICE_DESC" -> PRICE_DESC;
			case "CREATED_AT_DESC" -> CREATED_AT_DESC;
			default -> CREATED_AT_DESC;
		};
	}

	public Sort toSort() {
		return switch (this) {
			case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price");
			case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "price");
			case CREATED_AT_DESC -> Sort.by(Sort.Direction.DESC, "createdAt");
		};
	}
}