package notes.seller.service.integration.storage;

public class NoopStorageService implements StorageService {
	@Override
	public PresignedUrl createUploadUrl(StorageUploadRequest request) {
		throw new IllegalStateException("Storage is disabled. Configure app.storage.s3.* to enable uploads.");
	}

	@Override
	public PresignedUrl createDownloadUrl(StorageDownloadRequest request) {
		throw new IllegalStateException("Storage is disabled. Configure app.storage.s3.* to enable downloads.");
	}
}