package notes.seller.service.persistence.commerce;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import notes.seller.service.domain.commerce.ItemType;
import notes.seller.service.persistence.common.BaseEntity;
import notes.seller.service.persistence.identity.UserEntity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "entitlements", indexes = {
		@Index(name = "idx_entitlements_client", columnList = "client_id")
}, uniqueConstraints = {
		@UniqueConstraint(name = "uk_entitlements_client_item", columnNames = {"client_id", "item_type", "item_id"})
})
public class EntitlementEntity extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "client_id", nullable = false)
	private UserEntity client;

	@Enumerated(EnumType.STRING)
	@Column(name = "item_type", nullable = false, length = 20)
	private ItemType itemType;

	@Column(name = "item_id", nullable = false)
	private UUID itemId;

	@Column(name = "granted_at", nullable = false)
	private Instant grantedAt;
}