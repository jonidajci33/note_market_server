package notes.seller.service.persistence.catalog;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CourseRepository extends JpaRepository<CourseEntity, UUID>, JpaSpecificationExecutor<CourseEntity> {
	Optional<CourseEntity> findByIdAndSellerId(UUID id, UUID sellerId);
	boolean existsByNicheId(UUID nicheId);
}