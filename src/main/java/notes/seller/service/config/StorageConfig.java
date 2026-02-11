package notes.seller.service.config;

import java.net.URI;
import notes.seller.service.integration.storage.NoopStorageService;
import notes.seller.service.integration.storage.S3Properties;
import notes.seller.service.integration.storage.S3StorageService;
import notes.seller.service.integration.storage.StorageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class StorageConfig {

	@Bean
	@ConditionalOnProperty(prefix = "app.storage.s3", name = "enabled", havingValue = "true")
	public S3Presigner s3Presigner(S3Properties properties) {
		S3Presigner.Builder builder = S3Presigner.builder()
				.region(Region.of(properties.region()))
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())));
		if (properties.endpoint() != null && !properties.endpoint().isBlank()) {
			builder.endpointOverride(URI.create(properties.endpoint()));
		}
		return builder.build();
	}

	@Bean
	@ConditionalOnBean(S3Presigner.class)
	public StorageService s3StorageService(S3Presigner presigner, S3Properties properties) {
		return new S3StorageService(presigner, properties);
	}

	@Bean
	@ConditionalOnMissingBean(StorageService.class)
	public StorageService noopStorageService() {
		return new NoopStorageService();
	}
}