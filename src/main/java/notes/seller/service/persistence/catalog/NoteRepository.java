package notes.seller.service.persistence.catalog;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import notes.seller.service.domain.catalog.NoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NoteRepository extends JpaRepository<NoteEntity, UUID>, JpaSpecificationExecutor<NoteEntity> {
	@EntityGraph(attributePaths = {"niche", "niche.category", "tags"})
	Optional<NoteEntity> findByIdAndSellerId(UUID id, UUID sellerId);

	@EntityGraph(attributePaths = {"niche", "niche.category", "tags"})
	List<NoteEntity> findBySellerIdOrderByCreatedAtDesc(UUID sellerId);

	long countBySellerIdAndStatus(UUID sellerId, NoteStatus status);

	@EntityGraph(attributePaths = {"niche", "niche.category", "tags"})
	Page<NoteEntity> findByStatus(NoteStatus status, Pageable pageable);

	@EntityGraph(attributePaths = {"niche", "niche.category", "tags"})
	Page<NoteEntity> findByStatusIn(Collection<NoteStatus> statuses, Pageable pageable);

	boolean existsByNicheId(UUID nicheId);

	@EntityGraph(attributePaths = {"niche", "niche.category", "seller", "seller.sellerProfile", "tags"})
	@Override
	Optional<NoteEntity> findById(UUID id);
}