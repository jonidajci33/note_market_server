package notes.seller.service.support;

import static org.assertj.core.api.Assertions.assertThat;

import notes.seller.service.persistence.common.BaseEntity;

public final class DbAssertions {
	private DbAssertions() {
	}

	public static void assertPersisted(BaseEntity entity) {
		assertThat(entity.getId()).isNotNull();
		assertThat(entity.getCreatedAt()).isNotNull();
		assertThat(entity.getUpdatedAt()).isNotNull();
	}
}
