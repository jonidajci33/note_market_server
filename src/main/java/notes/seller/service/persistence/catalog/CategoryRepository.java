package notes.seller.service.persistence.catalog;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
	Optional<CategoryEntity> findBySlug(String slug);
}
