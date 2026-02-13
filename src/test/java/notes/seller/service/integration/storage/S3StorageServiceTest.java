package notes.seller.service.integration.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

class S3StorageServiceTest {

	@AfterEach
	void clearRequestContext() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void createUploadUrl_usesRequestHostWhenPublicEndpointTemplateConfigured() {
		S3Properties properties = new S3Properties(
				true,
				"http://localhost:9000",
				"http://{request-host}:9000",
				"us-east-1",
				"notes",
				"minioadmin",
				"minioadmin",
				Duration.ofMinutes(15)
		);
		S3Presigner defaultPresigner = presignerFor("http://localhost:9000", properties);
		try {
			S3StorageService service = new S3StorageService(defaultPresigner, properties);

			MockHttpServletRequest request = new MockHttpServletRequest();
			request.setServerName("192.168.64.246");
			request.setServerPort(8080);
			RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

			PresignedUrl uploadUrl = service.createUploadUrl(new StorageUploadRequest("notes/key.pdf", "application/pdf"));

			assertThat(URI.create(uploadUrl.url()).getHost()).isEqualTo("192.168.64.246");
		} finally {
			defaultPresigner.close();
		}
	}

	@Test
	void createUploadUrl_fallsBackToConfiguredEndpointWhenPublicEndpointMissing() {
		S3Properties properties = new S3Properties(
				true,
				"http://localhost:9000",
				null,
				"us-east-1",
				"notes",
				"minioadmin",
				"minioadmin",
				Duration.ofMinutes(15)
		);
		S3Presigner defaultPresigner = presignerFor("http://localhost:9000", properties);
		try {
			S3StorageService service = new S3StorageService(defaultPresigner, properties);

			PresignedUrl uploadUrl = service.createUploadUrl(new StorageUploadRequest("notes/key.pdf", "application/pdf"));

			assertThat(URI.create(uploadUrl.url()).getHost()).endsWith("localhost");
		} finally {
			defaultPresigner.close();
		}
	}

	@Test
	void createUploadUrl_usesRequestHostWhenPublicEndpointMissingAndEndpointIsInternalMinioHost() {
		S3Properties properties = new S3Properties(
				true,
				"http://notes.minio:9000",
				null,
				"us-east-1",
				"notes",
				"minioadmin",
				"minioadmin",
				Duration.ofMinutes(15)
		);
		S3Presigner defaultPresigner = presignerFor("http://notes.minio:9000", properties);
		try {
			S3StorageService service = new S3StorageService(defaultPresigner, properties);

			MockHttpServletRequest request = new MockHttpServletRequest();
			request.setServerName("192.168.64.246");
			request.setServerPort(8080);
			RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

			PresignedUrl uploadUrl = service.createUploadUrl(new StorageUploadRequest("notes/key.pdf", "application/pdf"));

			assertThat(URI.create(uploadUrl.url()).getHost()).isEqualTo("192.168.64.246");
			assertThat(URI.create(uploadUrl.url()).getPort()).isEqualTo(9000);
		} finally {
			defaultPresigner.close();
		}
	}

	@Test
	void createUploadUrl_keepsConfiguredEndpointWhenPublicEndpointMissingAndEndpointIsPublic() {
		S3Properties properties = new S3Properties(
				true,
				"https://s3.amazonaws.com",
				null,
				"us-east-1",
				"notes",
				"minioadmin",
				"minioadmin",
				Duration.ofMinutes(15)
		);
		S3Presigner defaultPresigner = presignerFor("https://s3.amazonaws.com", properties);
		try {
			S3StorageService service = new S3StorageService(defaultPresigner, properties);

			MockHttpServletRequest request = new MockHttpServletRequest();
			request.setServerName("192.168.64.246");
			request.setServerPort(8080);
			RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

			PresignedUrl uploadUrl = service.createUploadUrl(new StorageUploadRequest("notes/key.pdf", "application/pdf"));

			assertThat(URI.create(uploadUrl.url()).getHost()).endsWith("s3.amazonaws.com");
		} finally {
			defaultPresigner.close();
		}
	}

	@Test
	void createUploadUrl_usesPathStyleBucketAddressing() {
		S3Properties properties = new S3Properties(
				true,
				"http://minio:9000",
				null,
				"us-east-1",
				"notes",
				"minioadmin",
				"minioadmin",
				Duration.ofMinutes(15)
		);
		S3Presigner defaultPresigner = presignerFor("http://minio:9000", properties);
		try {
			S3StorageService service = new S3StorageService(defaultPresigner, properties);

			PresignedUrl uploadUrl = service.createUploadUrl(new StorageUploadRequest("test/key.pdf", "application/pdf"));

			URI uri = URI.create(uploadUrl.url());
			// Path-style: host should be plain "minio", NOT "notes.minio"
			assertThat(uri.getHost()).isEqualTo("minio");
			// Bucket name should be in the path
			assertThat(uri.getPath()).startsWith("/notes/");
		} finally {
			defaultPresigner.close();
		}
	}

	private S3Presigner presignerFor(String endpoint, S3Properties properties) {
		return S3Presigner.builder()
				.region(Region.of(properties.region()))
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())))
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
				.endpointOverride(URI.create(endpoint))
				.build();
	}
}
