package notes.seller.service.application.catalog;

import notes.seller.service.integration.storage.PresignedUrl;

public record UploadSession(String fileKey, PresignedUrl presignedUrl) {
}