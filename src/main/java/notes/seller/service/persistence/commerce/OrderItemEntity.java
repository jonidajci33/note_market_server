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
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import notes.seller.service.domain.commerce.ItemType;
import notes.seller.service.persistence.common.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "order_items", indexes = {
		@Index(name = "idx_order_items_order", columnList = "order_id")
})
public class OrderItemEntity extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private OrderEntity order;

	@Enumerated(EnumType.STRING)
	@Column(name = "item_type", nullable = false, length = 20)
	private ItemType itemType;

	@Column(name = "item_id", nullable = false)
	private UUID itemId;

	@Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal unitPrice;
}