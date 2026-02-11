package notes.seller.service.integration.storage;

import java.time.Duration;
import java.time.Instant;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

public class S3StorageService implements StorageService {
	private final S3Presigner presigner;
	private final S3Properties properties;

	public S3StorageService(S3Presigner presigner, S3Properties properties) {
		this.presigner = presigner;
		this.properties = properties;
	}

	@Override
	public PresignedUrl createUploadUrl(StorageUploadRequest request) {
		Duration expiration = defaultExpiration();
		PutObjectRequest.Builder putBuilder = PutObjectRequest.builder()
				.bucket(properties.bucket())
				.key(request.key());
		if (request.contentType() != null && !request.contentType().isBlank()) {
			putBuilder.contentType(request.contentType());
		}
		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
				.signatureDuration(expiration)
				.putObjectRequest(putBuilder.build())
				.build();
		PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);
		return new PresignedUrl(presigned.url().toString(), Instant.now().plus(expiration));
	}

	@Override
	public PresignedUrl createDownloadUrl(StorageDownloadRequest request) {
		Duration expiration = defaultExpiration();
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.bucket(properties.bucket())
				.key(request.key())
				.build();
		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
				.signatureDuration(expiration)
				.getObjectRequest(getObjectRequest)
				.build();
		PresignedGetObjectRequest presigned = presigner.presignGetObject(presignRequest);
		return new PresignedUrl(presigned.url().toString(), Instant.now().plus(expiration));
	}

	private Duration defaultExpiration() {
		if (properties.presignExpiration() != null) {
			return properties.presignExpiration();
		}
		return Duration.ofMinutes(15);
	}
}