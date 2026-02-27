package notes.seller.service.web.catalog.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import notes.seller.service.domain.catalog.NoteStatus;

public record NoteDetailResponse(
		UUID id,
		UUID sellerId,
		UUID nicheId,
		UUID categoryId,
		String title,
		String description,
		String coverImageUrl,
		BigDecimal price,
		NoteStatus status,
		Set<TagInfo> tags,
		Instant createdAt,
		Integer pages,
		String contentType,
		SellerInfo seller,
		NicheInfo niche,
		Double averageRating,
		int ratingCount
) {
	public record SellerInfo(
			UUID id,
			String displayName,
			long noteCount
	) {}

	public record NicheInfo(
			UUID id,
			String name,
			String slug
	) {}
}
