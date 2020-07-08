package com.usepipeline.portal.database.inventory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface InventoryRepository extends JpaRepository<InventoryEntity, UUID> {
    @Query("SELECT DISTINCT inventory" +
            " FROM InventoryEntity inventory" +
            " WHERE (" +
            "   inventory.userId IS NULL" +
            "   OR inventory.userId = :userId" +
            " )" +
            " OR inventory.organizationAccountId = :orgAcctId"
    )
    Page<InventoryEntity> findAccessibleInventories(@Param("orgAcctId") UUID orgAcctId, @Param("userId") UUID userId, Pageable pageable);

}