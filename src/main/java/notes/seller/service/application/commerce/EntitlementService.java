package notes.seller.service.application.commerce;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import notes.seller.service.domain.commerce.ItemType;
import notes.seller.service.persistence.commerce.EntitlementEntity;
import notes.seller.service.persistence.commerce.EntitlementRepository;
import notes.seller.service.persistence.identity.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EntitlementService {
	private final EntitlementRepository entitlementRepository;

	public EntitlementService(EntitlementRepository entitlementRepository) {
		this.entitlementRepository = entitlementRepository;
	}

	public boolean hasEntitlement(UUID clientId, ItemType itemType, UUID itemId) {
		return entitlementRepository.existsByClientIdAndItemTypeAndItemId(clientId, itemType, itemId);
	}

	public List<EntitlementEntity> listForClient(UUID clientId) {
		return entitlementRepository.findByClientId(clientId);
	}

	public void grantIfMissing(UserEntity client, ItemType itemType, UUID itemId) {
		if (entitlementRepository.existsByClientIdAndItemTypeAndItemId(client.getId(), itemType, itemId)) {
			return;
		}
		EntitlementEntity entitlement = new EntitlementEntity();
		entitlement.setClient(client);
		entitlement.setItemType(itemType);
		entitlement.setItemId(itemId);
		entitlement.setGrantedAt(Instant.now());
		entitlementRepository.save(entitlement);
	}
}