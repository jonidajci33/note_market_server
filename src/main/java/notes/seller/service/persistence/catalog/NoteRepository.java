package notes.seller.service.persistence.catalog;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import notes.seller.service.domain.catalog.NoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NoteRepository extends JpaRepository<NoteEntity, UUID>, JpaSpecificationExecutor<NoteEntity> {
	Optional<NoteEntity> findByIdAndSellerId(UUID id, UUID sellerId);

	List<NoteEntity> findByCourseId(UUID courseId);

	List<NoteEntity> findBySellerIdOrderByCreatedAtDesc(UUID sellerId);

	long countBySellerIdAndStatus(UUID sellerId, NoteStatus status);

	Page<NoteEntity> findByStatus(NoteStatus status, Pageable pageable);

	Page<NoteEntity> findByStatusIn(Collection<NoteStatus> statuses, Pageable pageable);

	boolean existsByNicheId(UUID nicheId);
}