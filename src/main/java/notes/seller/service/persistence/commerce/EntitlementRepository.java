package notes.seller.service.persistence.commerce;

import java.util.List;
import java.util.UUID;
import notes.seller.service.domain.commerce.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntitlementRepository extends JpaRepository<EntitlementEntity, UUID> {
	boolean existsByClientIdAndItemTypeAndItemId(UUID clientId, ItemType itemType, UUID itemId);

	List<EntitlementEntity> findByClientId(UUID clientId);
}