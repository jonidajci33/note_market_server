package notes.seller.service.web.delivery.dto;

import java.time.Instant;

public record DownloadUrlResponse(String downloadUrl, Instant expiresAt) {
}