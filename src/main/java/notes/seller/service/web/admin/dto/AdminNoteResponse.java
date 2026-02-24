package notes.seller.service.web.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import notes.seller.service.domain.catalog.NoteStatus;

public record AdminNoteResponse(
		UUID id,
		String title,
		String description,
		String coverImageUrl,
		BigDecimal price,
		NoteStatus status,
		Instant createdAt,
		UUID sellerId,
		String sellerDisplayName,
		UUID nicheId,
		String nicheName,
		int submissionCount,
		int remainingSubmissions,
		String rejectionReason,
		UUID reviewedBy,
		Instant reviewedAt
) {
}
