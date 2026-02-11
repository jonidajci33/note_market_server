package notes.seller.service.persistence.commerce;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import notes.seller.service.domain.commerce.OrderStatus;
import notes.seller.service.persistence.common.BaseEntity;
import notes.seller.service.persistence.identity.UserEntity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders", indexes = {
		@Index(name = "idx_orders_client", columnList = "client_id"),
		@Index(name = "idx_orders_status", columnList = "status"),
		@Index(name = "idx_orders_created_at", columnList = "created_at")
})
public class OrderEntity extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "client_id", nullable = false)
	private UserEntity client;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus status = OrderStatus.PENDING;

	@Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalAmount;

	@Column(nullable = false, length = 10)
	private String currency;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<OrderItemEntity> items = new ArrayList<>();

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PaymentEntity> payments = new ArrayList<>();
}