package ai.salesfox.portal.database.inventory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface InventoryRepository extends JpaRepository<InventoryEntity, UUID> {
    @Query("SELECT inventory" +
            " FROM InventoryEntity inventory" +
            " LEFT JOIN inventory.inventoryOrganizationAccountRestrictionEntity orgAcctRestriction" +
            " LEFT JOIN inventory.inventoryUserRestrictionEntity userRestriction" +
            " WHERE (" +
            "   (orgAcctRestriction != NULL AND orgAcctRestriction.organizationAccountId = :orgAcctId)" +
            "   OR" +
            "   (userRestriction != NULL AND userRestriction.userId = :userId)" +
            " )"
    )
    Page<InventoryEntity> findAccessibleInventories(@Param("orgAcctId") UUID orgAcctId, @Param("userId") UUID userId, Pageable pageable);

}
