package notes.seller.service.persistence.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import notes.seller.service.persistence.common.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categories", indexes = {
	@Index(name = "idx_categories_slug", columnList = "slug")
})
public class CategoryEntity extends BaseEntity {
	@Column(nullable = false, unique = true, length = 120)
	private String slug;

	@Column(nullable = false, length = 120)
	private String name;
}
