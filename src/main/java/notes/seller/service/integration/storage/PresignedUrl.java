package notes.seller.service.integration.storage;

import java.time.Instant;

public record PresignedUrl(String url, Instant expiresAt) {
}