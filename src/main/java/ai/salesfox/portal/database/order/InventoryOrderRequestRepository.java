package ai.salesfox.portal.database.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface InventoryOrderRequestRepository extends JpaRepository<InventoryOrderRequestEntity, UUID> {
}
