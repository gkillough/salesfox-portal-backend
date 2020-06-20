package com.usepipeline.portal.database.inventory.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface InventoryItemRepository extends JpaRepository<InventoryItemEntity, InventoryItemPK> {
    Page<InventoryItemEntity> findByInventoryId(UUID inventoryId, Pageable pageable);

    @Query("SELECT COUNT(item.catalogueItemId) FROM InventoryItemEntity item WHERE item.inventoryId = ?1 AND item.quantity > 0")
    Long countItemsWithNonZeroQuantity(UUID inventoryId);

}