package com.usepipeline.portal.database.inventory.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface InventoryItemRepository extends JpaRepository<InventoryItemEntity, InventoryItemPK> {

}