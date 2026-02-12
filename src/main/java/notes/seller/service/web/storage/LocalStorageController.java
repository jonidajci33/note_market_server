package notes.seller.service.web.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import notes.seller.service.integration.storage.LocalFileStorageService;
import notes.seller.service.integration.storage.LocalStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/storage/local")
@ConditionalOnProperty(prefix = "app.storage.local", name = "enabled", havingValue = "true")
public class LocalStorageController {
	private static final Logger log = LoggerFactory.getLogger(LocalStorageController.class);
	private final LocalFileStorageService storageService;
	private final LocalStorageProperties localProperties;

	public LocalStorageController(LocalFileStorageService storageService, LocalStorageProperties localProperties) {
		this.storageService = storageService;
		this.localProperties = localProperties;
	}

	@PutMapping(value = "/upload/{token}", consumes = MediaType.ALL_VALUE)
	public ResponseEntity<Void> upload(@PathVariable("token") String token, HttpServletRequest request) {
		LocalFileStorageService.UploadTokenData tokenData = storageService.consumeUploadToken(token);
		try (InputStream inputStream = request.getInputStream()) {
			storageService.writeFile(tokenData.path(), inputStream);
			log.info("Local storage upload completed: key={} path={} rootDir={}",
					tokenData.key(), tokenData.path(), localProperties.rootDir());
			return ResponseEntity.ok().build();
		} catch (IOException ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read upload body", ex);
		}
	}

	@GetMapping("/download/{token}")
	public ResponseEntity<InputStreamResource> download(@PathVariable("token") String token,
												 @RequestParam("key") String key) {
		LocalFileStorageService.DownloadTokenData tokenData = storageService.validateDownloadToken(token, key);
		Path path = tokenData.path();
		if (!Files.exists(path) || !Files.isRegularFile(path)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Stored file not found");
		}

		try {
			long contentLength = Files.size(path);
			InputStreamResource resource = new InputStreamResource(Files.newInputStream(path));
			String contentType = Files.probeContentType(path);
			if (contentType == null || contentType.isBlank()) {
				contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
			}
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(contentType))
					.contentLength(contentLength)
					.header(HttpHeaders.CACHE_CONTROL, "no-store")
					.body(resource);
		} catch (IOException ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read stored file", ex);
		}
	}
}
