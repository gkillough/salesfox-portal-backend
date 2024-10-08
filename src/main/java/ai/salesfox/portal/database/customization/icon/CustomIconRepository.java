package ai.salesfox.portal.database.customization.icon;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface CustomIconRepository extends JpaRepository<CustomIconEntity, UUID> {
    @Query("SELECT icon" +
            " FROM CustomIconEntity icon" +
            " LEFT JOIN icon.customIconOrganizationAccountRestrictionEntity orgAcctRestriction" +
            " WHERE (" +
            "   orgAcctRestriction != NULL " +
            "   AND orgAcctRestriction.organizationAccountId = :orgAcctId" +
            " )"
    )
    Page<CustomIconEntity> findAccessibleCustomIcons(@Param("orgAcctId") UUID orgAcctId, Pageable pageable);

}
