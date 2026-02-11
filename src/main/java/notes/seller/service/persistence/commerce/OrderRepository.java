package notes.seller.service.persistence.commerce;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
	Optional<OrderEntity> findByIdAndClientId(UUID id, UUID clientId);
}