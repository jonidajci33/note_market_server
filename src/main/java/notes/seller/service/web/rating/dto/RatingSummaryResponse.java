package notes.seller.service.web.rating.dto;

public record RatingSummaryResponse(
		Double averageRating,
		long ratingCount
) {}
