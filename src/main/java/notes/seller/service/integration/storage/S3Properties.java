package notes.seller.service.integration.storage;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage.s3")
public record S3Properties(
		boolean enabled,
		String endpoint,
		String region,
		String bucket,
		String accessKey,
		String secretKey,
		Duration presignExpiration
) {
}