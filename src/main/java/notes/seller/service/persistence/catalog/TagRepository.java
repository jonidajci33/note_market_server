package notes.seller.service.persistence.catalog;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagRepository extends JpaRepository<TagEntity, UUID> {
	Optional<TagEntity> findBySlug(String slug);

	boolean existsBySlug(String slug);

	List<TagEntity> findAllByOrderByNameAsc();

	@Query(value = "SELECT t.* FROM note_seller.tags t JOIN note_seller.niche_tags nt ON t.id = nt.tag_id WHERE nt.niche_id = :nicheId ORDER BY t.name", nativeQuery = true)
	List<TagEntity> findByNicheId(@Param("nicheId") UUID nicheId);

	Set<TagEntity> findByIdIn(Collection<UUID> ids);

	@Query("SELECT COUNT(n) > 0 FROM NoteEntity n JOIN n.tags t WHERE t.id = :tagId")
	boolean existsNoteWithTag(@Param("tagId") UUID tagId);
}
