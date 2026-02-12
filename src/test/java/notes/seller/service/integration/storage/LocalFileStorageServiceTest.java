package notes.seller.service.integration.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

class LocalFileStorageServiceTest {

	@TempDir
	Path tempDir;

	@Test
	void shouldCreateUploadUrlPersistFileAndCreateDownloadUrl() throws Exception {
		LocalStorageProperties properties = new LocalStorageProperties(
				true,
			tempDir.toString(),
				"http://{request-host}:{request-port}",
				Duration.ofMinutes(10),
				Duration.ofMinutes(10)
		);
		LocalFileStorageService service = new LocalFileStorageService(properties);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("192.168.0.50");
		request.setServerPort(8080);
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		try {
			PresignedUrl upload = service.createUploadUrl(new StorageUploadRequest("notes/u1/n1/file.pdf", "application/pdf"));
			String uploadToken = tokenFromPath(upload.url());
			LocalFileStorageService.UploadTokenData uploadData = service.consumeUploadToken(uploadToken);
			service.writeFile(uploadData.path(), new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));

			PresignedUrl download = service.createDownloadUrl(new StorageDownloadRequest("notes/u1/n1/file.pdf"));
			String downloadToken = tokenFromPath(download.url());
			LocalFileStorageService.DownloadTokenData downloadData =
					service.validateDownloadToken(downloadToken, "notes/u1/n1/file.pdf");
			String content = Files.readString(downloadData.path());

			assertThat(upload.url()).contains("http://192.168.0.50:8080/api/v1/storage/local/upload/");
			assertThat(download.url()).contains("http://192.168.0.50:8080/api/v1/storage/local/download/");
			assertThat(content).isEqualTo("hello");
		} finally {
			RequestContextHolder.resetRequestAttributes();
		}
	}

	@Test
	void shouldRejectTraversalKey() {
		LocalStorageProperties properties = new LocalStorageProperties(
				true,
			tempDir.toString(),
				"http://localhost:8080",
				Duration.ofMinutes(10),
				Duration.ofMinutes(10)
		);
		LocalFileStorageService service = new LocalFileStorageService(properties);

		assertThatThrownBy(() -> service.createUploadUrl(new StorageUploadRequest("../evil.txt", "text/plain")))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Invalid storage key path");
	}

	private String tokenFromPath(String url) {
		String path = URI.create(url).getPath();
		return path.substring(path.lastIndexOf('/') + 1);
	}
}
