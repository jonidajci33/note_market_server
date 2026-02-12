package notes.seller.service.persistence.catalog;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import notes.seller.service.domain.catalog.NoteStatus;
import notes.seller.service.persistence.common.BaseEntity;
import notes.seller.service.persistence.identity.UserEntity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notes", indexes = {
		@Index(name = "idx_notes_niche", columnList = "niche_id"),
		@Index(name = "idx_notes_seller", columnList = "seller_id"),
		@Index(name = "idx_notes_status", columnList = "status"),
		@Index(name = "idx_notes_created_at", columnList = "created_at")
})
public class NoteEntity extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id")
	private CourseEntity course;

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

	@Column(precision = 10, scale = 2)
	private BigDecimal price;

	@Column(name = "file_key", length = 512)
	private String fileKey;

	@Column(name = "content_type", length = 120)
	private String contentType;

	@Column(name = "cover_file_key", length = 512)
	private String coverFileKey;

	@Column(name = "cover_content_type", length = 120)
	private String coverContentType;

	@Column(name = "file_size")
	private Long fileSize;

	@Column(name = "checksum_sha256", length = 128)
	private String checksumSha256;

	@Column
	private Integer pages;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private NoteStatus status = NoteStatus.DRAFT;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "note_tags", joinColumns = @JoinColumn(name = "note_id"))
	@Column(name = "tag")
	private Set<String> tags = new HashSet<>();
}
