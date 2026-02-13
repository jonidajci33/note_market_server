package notes.seller.service.integration.storage;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
		S3Presigner activePresigner = resolvePresignerForCurrentRequest();
		try {
			PresignedPutObjectRequest presigned = activePresigner.presignPutObject(presignRequest);
			return new PresignedUrl(presigned.url().toString(), Instant.now().plus(expiration));
		} finally {
			closeIfTemporary(activePresigner);
		}
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
		S3Presigner activePresigner = resolvePresignerForCurrentRequest();
		try {
			PresignedGetObjectRequest presigned = activePresigner.presignGetObject(presignRequest);
			return new PresignedUrl(presigned.url().toString(), Instant.now().plus(expiration));
		} finally {
			closeIfTemporary(activePresigner);
		}
	}

	private S3Presigner resolvePresignerForCurrentRequest() {
		String endpoint = resolvedSigningEndpoint();
		if (endpoint == null || endpoint.isBlank()) {
			return presigner;
		}
		String configured = properties.endpoint();
		if (configured != null && configured.equals(endpoint)) {
			return presigner;
		}
		return S3Presigner.builder()
				.region(Region.of(properties.region()))
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())))
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
				.endpointOverride(URI.create(endpoint))
				.build();
	}

	private String resolvedSigningEndpoint() {
		String candidate = properties.publicEndpoint();
		if (candidate == null || candidate.isBlank()) {
			return fallbackEndpointUsingRequestHostForInternalEndpoint();
		}
		if (!candidate.contains("{request-host}") && !candidate.contains("{request-port}")) {
			return candidate;
		}
		if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
			String host = attributes.getRequest().getServerName();
			String port = Integer.toString(attributes.getRequest().getServerPort());
			return candidate
					.replace("{request-host}", host)
					.replace("{request-port}", port);
		}
		return candidate
				.replace("{request-host}", "localhost")
				.replace("{request-port}", "8080");
	}

	private String fallbackEndpointUsingRequestHostForInternalEndpoint() {
		String endpoint = properties.endpoint();
		if (endpoint == null || endpoint.isBlank()) {
			return endpoint;
		}
		URI endpointUri = parseUri(endpoint);
		if (endpointUri == null) {
			return endpoint;
		}
		String endpointHost = endpointUri.getHost();
		if (endpointHost == null || endpointHost.isBlank() || !isInternalHost(endpointHost)) {
			return endpoint;
		}
		if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
			return endpoint;
		}
		String requestHost = attributes.getRequest().getServerName();
		if (requestHost == null || requestHost.isBlank()) {
			return endpoint;
		}
		return withReplacedHost(endpointUri, requestHost);
	}

	private URI parseUri(String raw) {
		try {
			return URI.create(raw);
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private boolean isInternalHost(String host) {
		String normalized = host.toLowerCase();
		return normalized.equals("localhost")
				|| normalized.equals("127.0.0.1")
				|| normalized.equals("::1")
				|| normalized.equals("minio")
				|| normalized.endsWith(".minio");
	}

	private String withReplacedHost(URI baseUri, String host) {
		String scheme = baseUri.getScheme() == null ? "http" : baseUri.getScheme();
		try {
			return new URI(
					scheme,
					baseUri.getUserInfo(),
					host,
					baseUri.getPort(),
					baseUri.getPath(),
					baseUri.getQuery(),
					baseUri.getFragment()
			).toString();
		} catch (URISyntaxException ex) {
			return baseUri.toString();
		}
	}

	private void closeIfTemporary(S3Presigner activePresigner) {
		if (activePresigner != presigner) {
			activePresigner.close();
		}
	}

	private Duration defaultExpiration() {
		if (properties.presignExpiration() != null) {
			return properties.presignExpiration();
		}
		return Duration.ofMinutes(15);
	}
}
