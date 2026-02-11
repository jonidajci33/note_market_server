package notes.seller.service.persistence.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "seller_profile")
public class SellerProfileEntity {
	@Id
	private UUID userId;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "user_id")
	private UserEntity user;

	@Column(name = "display_name", nullable = false, length = 120)
	private String displayName;

	@Column(columnDefinition = "text")
	private String bio;

	@Column(name = "payout_info", columnDefinition = "text")
	private String payoutInfo;
}