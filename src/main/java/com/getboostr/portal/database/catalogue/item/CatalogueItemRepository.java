package com.getboostr.portal.database.catalogue.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface CatalogueItemRepository extends JpaRepository<CatalogueItemEntity, UUID> {
    @Query("SELECT DISTINCT itemRow" +
            " FROM CatalogueItemEntity itemRow" +
            " LEFT JOIN itemRow.catalogueItemRestrictionEntity restriction ON itemRow.itemId = restriction.itemId" +
            " WHERE (" +
            " itemRow.isActive = TRUE" +
            " AND (" +
            "   itemRow.restricted = FALSE" +
            "   OR (" +
            "       restriction.organizationAccountId = :orgAcctId" +
            "       AND (" +
            "         restriction.userId IS NULL" +
            "         OR restriction.userId = :userId" +
            "       )" +
            "     )" +
            "   )" +
            " )"
    )
    Page<CatalogueItemEntity> findAccessibleCatalogueItems(@Param("orgAcctId") UUID orgAcctId, @Param("userId") UUID userId, Pageable pageable);

}
