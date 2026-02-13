package notes.seller.service.persistence.catalog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NoteRepository extends JpaRepository<NoteEntity, UUID>, JpaSpecificationExecutor<NoteEntity> {
	Optional<NoteEntity> findByIdAndSellerId(UUID id, UUID sellerId);

	List<NoteEntity> findByCourseId(UUID courseId);

	List<NoteEntity> findBySellerIdOrderByCreatedAtDesc(UUID sellerId);
}