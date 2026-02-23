package notes.seller.service.persistence.rating;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.persistence.common.BaseEntity;
import notes.seller.service.persistence.identity.UserEntity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "note_ratings")
public class RatingEntity extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "note_id", nullable = false)
	private NoteEntity note;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@Column(nullable = false)
	private int rating;

	@Column(name = "review_text", columnDefinition = "text")
	private String reviewText;
}
