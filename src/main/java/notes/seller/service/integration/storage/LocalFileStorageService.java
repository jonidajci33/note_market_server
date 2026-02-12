package notes.seller.service.integration.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.http.HttpStatus;

public class LocalFileStorageService implements StorageService {
	private final LocalStorageProperties properties;
	private final Path rootDirectory;
	private final Map<String, UploadTokenData> uploadTokens = new ConcurrentHashMap<>();
	private final Map<String, DownloadTokenData> downloadTokens = new ConcurrentHashMap<>();

	public LocalFileStorageService(LocalStorageProperties properties) {
		this.properties = properties;
		this.rootDirectory = resolveRootDirectory(properties.rootDir());
	}

	@Override
	public PresignedUrl createUploadUrl(StorageUploadRequest request) {
		Path targetPath = resolvePath(request.key());
		Instant expiresAt = Instant.now().plus(uploadExpiration());
		String token = UUID.randomUUID().toString();
		uploadTokens.put(token, new UploadTokenData(request.key(), targetPath, request.contentType(), expiresAt));
		return new PresignedUrl(resolvedBaseUrl() + "/api/v1/storage/local/upload/" + token, expiresAt);
	}

	@Override
	public PresignedUrl createDownloadUrl(StorageDownloadRequest request) {
		Path targetPath = resolvePath(request.key());
		Instant expiresAt = Instant.now().plus(downloadExpiration());
		String token = UUID.randomUUID().toString();
		downloadTokens.put(token, new DownloadTokenData(request.key(), targetPath, expiresAt));
		String encodedKey = URLEncoder.encode(request.key(), StandardCharsets.UTF_8);
		return new PresignedUrl(resolvedBaseUrl() + "/api/v1/storage/local/download/" + token + "?key=" + encodedKey, expiresAt);
	}

	public UploadTokenData consumeUploadToken(String token) {
		purgeExpiredTokens();
		UploadTokenData data = uploadTokens.remove(token);
		if (data == null || Instant.now().isAfter(data.expiresAt())) {
			throw new ResponseStatusException(HttpStatus.GONE, "Upload session expired or invalid");
		}
		return data;
	}

	public DownloadTokenData validateDownloadToken(String token, String key) {
		purgeExpiredTokens();
		DownloadTokenData data = downloadTokens.get(token);
		if (data == null || Instant.now().isAfter(data.expiresAt())) {
			throw new ResponseStatusException(HttpStatus.GONE, "Download session expired or invalid");
		}
		if (!data.key().equals(key)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Download key mismatch");
		}
		return data;
	}

	public void writeFile(Path path, InputStream stream) {
		try {
			Path parent = path.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Files.copy(stream, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file", ex);
		}
	}

	public boolean isStorageAccessible() {
		try {
			Files.createDirectories(rootDirectory);
			return Files.isDirectory(rootDirectory) && Files.isWritable(rootDirectory);
		} catch (IOException ex) {
			return false;
		}
	}

	private Duration uploadExpiration() {
		if (properties.uploadExpiration() != null) {
			return properties.uploadExpiration();
		}
		return Duration.ofMinutes(15);
	}

	private Duration downloadExpiration() {
		if (properties.downloadExpiration() != null) {
			return properties.downloadExpiration();
		}
		return Duration.ofMinutes(15);
	}

	private Path resolvePath(String key) {
		if (key == null || key.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Storage key is required");
		}
		Path relative = Paths.get(key).normalize();
		if (relative.isAbsolute() || relative.startsWith("..")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid storage key path");
		}
		Path resolved = rootDirectory.resolve(relative).normalize();
		if (!resolved.startsWith(rootDirectory)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid storage key path");
		}
		return resolved;
	}

	private Path resolveRootDirectory(String configuredRootDir) {
		String candidate = configuredRootDir;
		if (candidate == null || candidate.isBlank()) {
			candidate = ".storage-local";
		}
		Path root = Paths.get(candidate).toAbsolutePath().normalize();
		try {
			Files.createDirectories(root);
		} catch (IOException ex) {
			throw new IllegalStateException("Unable to initialize local storage directory: " + root, ex);
		}
		return root;
	}

	private String resolvedBaseUrl() {
		String configured = properties.publicBaseUrl();
		if (configured == null || configured.isBlank()) {
			configured = "http://{request-host}:{request-port}";
		}
		if (!configured.contains("{request-host}") && !configured.contains("{request-port}")) {
			return stripTrailingSlash(configured);
		}
		if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
			String host = attributes.getRequest().getServerName();
			String port = Integer.toString(attributes.getRequest().getServerPort());
			return stripTrailingSlash(
					configured.replace("{request-host}", host).replace("{request-port}", port)
			);
		}
		return stripTrailingSlash(configured.replace("{request-host}", "localhost").replace("{request-port}", "8080"));
	}

	private String stripTrailingSlash(String value) {
		if (value.endsWith("/")) {
			return value.substring(0, value.length() - 1);
		}
		return value;
	}

	private void purgeExpiredTokens() {
		Instant now = Instant.now();
		uploadTokens.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expiresAt()));
		downloadTokens.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expiresAt()));
	}

	public record UploadTokenData(String key, Path path, String contentType, Instant expiresAt) {
	}

	public record DownloadTokenData(String key, Path path, Instant expiresAt) {
	}
}
