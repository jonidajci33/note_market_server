package notes.seller.service.persistence.catalog;

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
import notes.seller.service.domain.catalog.CourseStatus;
import notes.seller.service.persistence.common.BaseEntity;
import notes.seller.service.persistence.identity.UserEntity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "courses", indexes = {
		@Index(name = "idx_courses_niche", columnList = "niche_id"),
		@Index(name = "idx_courses_seller", columnList = "seller_id"),
		@Index(name = "idx_courses_status", columnList = "status"),
		@Index(name = "idx_courses_created_at", columnList = "created_at")
})
public class CourseEntity extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seller_id", nullable = false)
	private UserEntity seller;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "niche_id", nullable = false)
	private NicheEntity niche;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(columnDefinition = "text")
	private String description;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CourseStatus status = CourseStatus.DRAFT;
}