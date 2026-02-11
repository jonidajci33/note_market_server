package notes.seller.service.integration.storage;

public interface StorageService {
	PresignedUrl createUploadUrl(StorageUploadRequest request);

	PresignedUrl createDownloadUrl(StorageDownloadRequest request);
}