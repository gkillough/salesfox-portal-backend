package com.usepipeline.portal.database.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface InventoryRepository extends JpaRepository<InventoryEntity, UUID> {

}