package notes.seller.service.persistence.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import notes.seller.service.persistence.common.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "niches", indexes = {
		@Index(name = "idx_niches_slug", columnList = "slug"),
		@Index(name = "idx_niches_category", columnList = "category_id")
})
public class NicheEntity extends BaseEntity {
	@Column(nullable = false, unique = true, length = 120)
	private String slug;

	@Column(nullable = false, length = 120)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private CategoryEntity category;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "niche_tags",
			joinColumns = @JoinColumn(name = "niche_id"),
			inverseJoinColumns = @JoinColumn(name = "tag_id")
	)
	private Set<TagEntity> tags = new HashSet<>();
}
