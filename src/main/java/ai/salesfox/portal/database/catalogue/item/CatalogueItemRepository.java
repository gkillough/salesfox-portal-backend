package ai.salesfox.portal.database.catalogue.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface CatalogueItemRepository extends JpaRepository<CatalogueItemEntity, UUID> {
    @Query("SELECT itemRow" +
            " FROM CatalogueItemEntity itemRow" +
            " LEFT JOIN itemRow.catalogueItemOrganizationAccountRestrictionEntity orgAcctRestriction" +
            " LEFT JOIN itemRow.catalogueItemUserRestrictionEntity userRestriction" +
            " WHERE (" +
            "   (orgAcctRestriction = NULL AND userRestriction = NULL)" +
            "   OR" +
            "   (orgAcctRestriction != NULL AND orgAcctRestriction.organizationAccountId = :orgAcctId)" +
            "   OR" +
            "   (userRestriction != NULL AND userRestriction.userId = :userId)" +
            " )"
    )
    Page<CatalogueItemEntity> findAccessibleCatalogueItems(@Param("orgAcctId") UUID orgAcctId, @Param("userId") UUID userId, Pageable pageable);

}
