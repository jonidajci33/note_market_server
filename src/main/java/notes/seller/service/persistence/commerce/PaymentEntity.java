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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import notes.seller.service.domain.commerce.PaymentProvider;
import notes.seller.service.domain.commerce.PaymentStatus;
import notes.seller.service.persistence.common.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payments", indexes = {
		@Index(name = "idx_payments_order", columnList = "order_id")
})
public class PaymentEntity extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private OrderEntity order;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PaymentProvider provider;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PaymentStatus status;

	@Column(name = "provider_ref", length = 120)
	private String providerRef;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;
}