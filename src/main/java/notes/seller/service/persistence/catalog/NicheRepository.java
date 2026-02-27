package notes.seller.service.persistence.catalog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NicheRepository extends JpaRepository<NicheEntity, UUID> {
	Optional<NicheEntity> findBySlug(String slug);
	List<NicheEntity> findByCategoryId(UUID categoryId);
	boolean existsByCategoryId(UUID categoryId);

	@EntityGraph(attributePaths = {"category", "tags"})
	Optional<NicheEntity> findWithTagsById(UUID id);
}
