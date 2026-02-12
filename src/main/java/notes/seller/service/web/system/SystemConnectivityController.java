package notes.seller.service.web.system;

import notes.seller.service.integration.storage.LocalFileStorageService;
import notes.seller.service.integration.storage.LocalStorageProperties;
import notes.seller.service.integration.storage.S3Properties;
import notes.seller.service.web.system.dto.ConnectivityStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

@RestController
@RequestMapping("/api/v1/system")
public class SystemConnectivityController {
	private static final Logger log = LoggerFactory.getLogger(SystemConnectivityController.class);
	private final ObjectProvider<S3Client> s3ClientProvider;
	private final ObjectProvider<LocalFileStorageService> localStorageServiceProvider;
	private final S3Properties s3Properties;
	private final LocalStorageProperties localStorageProperties;

	public SystemConnectivityController(ObjectProvider<S3Client> s3ClientProvider,
										ObjectProvider<LocalFileStorageService> localStorageServiceProvider,
										S3Properties s3Properties,
										LocalStorageProperties localStorageProperties) {
		this.s3ClientProvider = s3ClientProvider;
		this.localStorageServiceProvider = localStorageServiceProvider;
		this.s3Properties = s3Properties;
		this.localStorageProperties = localStorageProperties;
	}

	@GetMapping("/connectivity")
	public ConnectivityStatusResponse connectivity() {
		if (localStorageProperties.enabled()) {
			LocalFileStorageService localStorageService = localStorageServiceProvider.getIfAvailable();
			boolean reachable = localStorageService != null && localStorageService.isStorageAccessible();
			String rootDir = localStorageProperties.rootDir();
			if (reachable) {
				log.info("Local storage connectivity check passed: rootDir={}", rootDir);
				return new ConnectivityStatusResponse(true, true, true, rootDir, "local-filesystem",
						"Backend can access local filesystem storage.");
			}
			log.warn("Local storage connectivity check failed: rootDir={} configured={}", rootDir, localStorageProperties.enabled());
			return new ConnectivityStatusResponse(true, true, false, rootDir, "local-filesystem",
					"Backend cannot access local filesystem storage.");
		}

		if (!s3Properties.enabled()) {
			log.info("Storage connectivity check: s3Enabled=false endpoint={} bucket={}",
					s3Properties.endpoint(), s3Properties.bucket());
			return new ConnectivityStatusResponse(true, false, false, s3Properties.endpoint(), s3Properties.bucket(),
					"S3 storage is disabled.");
		}

		S3Client s3Client = s3ClientProvider.getIfAvailable();
		if (s3Client == null) {
			log.warn("Storage connectivity check: s3 client not configured endpoint={} bucket={}",
					s3Properties.endpoint(), s3Properties.bucket());
			return new ConnectivityStatusResponse(true, true, false, s3Properties.endpoint(), s3Properties.bucket(),
					"S3 client is not configured.");
		}

		if (s3Properties.bucket() == null || s3Properties.bucket().isBlank()) {
			log.warn("Storage connectivity check: bucket missing endpoint={} bucket={}",
					s3Properties.endpoint(), s3Properties.bucket());
			return new ConnectivityStatusResponse(true, true, false, s3Properties.endpoint(), s3Properties.bucket(),
					"S3 bucket is not configured.");
		}

		try {
			s3Client.headBucket(HeadBucketRequest.builder().bucket(s3Properties.bucket()).build());
			log.info("Storage connectivity check passed: endpoint={} bucket={}", s3Properties.endpoint(), s3Properties.bucket());
			return new ConnectivityStatusResponse(true, true, true, s3Properties.endpoint(), s3Properties.bucket(),
					"Backend can reach MinIO bucket.");
		} catch (Exception ex) {
			log.warn("Storage connectivity check failed: endpoint={} bucket={} reason={}",
					s3Properties.endpoint(), s3Properties.bucket(), conciseMessage(ex));
			return new ConnectivityStatusResponse(true, true, false, s3Properties.endpoint(), s3Properties.bucket(),
					"Backend cannot reach MinIO bucket: " + conciseMessage(ex));
		}
	}

	private String conciseMessage(Exception ex) {
		String msg = ex.getMessage();
		if (msg == null || msg.isBlank()) {
			return ex.getClass().getSimpleName();
		}
		return msg;
	}
}
