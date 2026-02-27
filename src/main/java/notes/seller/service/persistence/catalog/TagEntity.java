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
@Table(name = "tags", indexes = {
		@Index(name = "idx_tags_slug", columnList = "slug"),
		@Index(name = "idx_tags_name", columnList = "name")
})
public class TagEntity extends BaseEntity {
	@Column(nullable = false, unique = true, length = 120)
	private String slug;

	@Column(nullable = false, length = 120)
	private String name;
}
