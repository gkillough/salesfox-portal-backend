package com.usepipeline.portal.database.order.status;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public interface InventoryOrderRequestStatusRepository extends JpaRepository<InventoryOrderRequestStatusEntity, UUID> {
    Optional<InventoryOrderRequestStatusEntity> findByOrderId(UUID orderId);

    List<InventoryOrderRequestStatusEntity> findAllByOrderIdIn(Collection<UUID> orderId);

}
