package ai.salesfox.portal.database.customization.branding_text;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface CustomBrandingTextRepository extends JpaRepository<CustomBrandingTextEntity, UUID> {
    @Query("SELECT text" +
            " FROM CustomBrandingTextEntity text" +
            " LEFT JOIN text.customBrandingTextOrgAccountRestrictionEntity orgAcctRestriction" +
            " WHERE (" +
            "   orgAcctRestriction != NULL " +
            "   AND orgAcctRestriction.orgAccountId = :orgAcctId" +
            " )"
    )
    Page<CustomBrandingTextEntity> findAccessibleCustomBrandingTexts(@Param("orgAcctId") UUID orgAcctId, Pageable pageable);

}
