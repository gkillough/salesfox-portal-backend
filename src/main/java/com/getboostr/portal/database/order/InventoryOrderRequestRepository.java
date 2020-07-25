package com.getboostr.portal.database.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface InventoryOrderRequestRepository extends JpaRepository<InventoryOrderRequestEntity, UUID> {
    Page<InventoryOrderRequestEntity> findByInventoryId(UUID inventoryId, Pageable pageable);

}
