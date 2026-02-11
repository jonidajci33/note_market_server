package notes.seller.service.web.catalog.dto;

import java.time.Instant;

public record UploadUrlResponse(String uploadUrl, String fileKey, Instant expiresAt) {
}