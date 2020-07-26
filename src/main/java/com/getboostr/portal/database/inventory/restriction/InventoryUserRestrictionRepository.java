package com.getboostr.portal.database.inventory.restriction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface InventoryUserRestrictionRepository extends JpaRepository<InventoryUserRestrictionEntity, UUID> {
}
