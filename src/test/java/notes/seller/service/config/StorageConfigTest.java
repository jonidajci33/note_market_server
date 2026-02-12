package notes.seller.service.config;

import static org.assertj.core.api.Assertions.assertThat;

import notes.seller.service.integration.storage.LocalFileStorageService;
import notes.seller.service.integration.storage.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class StorageConfigTest {
	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
			.withUserConfiguration(StorageConfig.class);

	@Test
	void shouldExposeSingleStorageServiceBeanWhenLocalStorageEnabled() {
		contextRunner
				.withPropertyValues(
						"app.storage.local.enabled=true",
						"app.storage.local.root-dir=target/test-local-storage",
						"app.storage.local.public-base-url=http://localhost:8080",
						"app.storage.s3.enabled=false"
				)
				.run(context -> {
					assertThat(context).hasSingleBean(StorageService.class);
					assertThat(context).hasSingleBean(LocalFileStorageService.class);
					assertThat(context.getBean(StorageService.class)).isInstanceOf(LocalFileStorageService.class);
				});
	}
}
