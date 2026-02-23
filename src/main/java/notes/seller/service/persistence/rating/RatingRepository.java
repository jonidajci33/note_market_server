package notes.seller.service.persistence.rating;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RatingRepository extends JpaRepository<RatingEntity, UUID> {
	Optional<RatingEntity> findByNoteIdAndUserId(UUID noteId, UUID userId);

	boolean existsByNoteIdAndUserId(UUID noteId, UUID userId);

	Page<RatingEntity> findByNoteIdOrderByCreatedAtDesc(UUID noteId, Pageable pageable);

	@Query("SELECT AVG(r.rating), COUNT(r) FROM RatingEntity r WHERE r.note.id = :noteId")
	Object[] findSummaryByNoteId(@Param("noteId") UUID noteId);

	@Query("SELECT r.note.id, AVG(r.rating), COUNT(r) FROM RatingEntity r WHERE r.note.id IN :noteIds GROUP BY r.note.id")
	List<Object[]> findSummariesByNoteIds(@Param("noteIds") Collection<UUID> noteIds);
}
