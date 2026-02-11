package notes.seller.service.persistence.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import notes.seller.service.persistence.common.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "niches", indexes = {
		@Index(name = "idx_niches_slug", columnList = "slug")
})
public class NicheEntity extends BaseEntity {
	@Column(nullable = false, unique = true, length = 120)
	private String slug;

	@Column(nullable = false, length = 120)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private NicheEntity parent;
}