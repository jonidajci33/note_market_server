package notes.seller.service.integration.storage;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage.local")
public record LocalStorageProperties(
		boolean enabled,
		String rootDir,
		String publicBaseUrl,
		Duration uploadExpiration,
		Duration downloadExpiration
) {
}
