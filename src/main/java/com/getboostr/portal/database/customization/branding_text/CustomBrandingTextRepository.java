package com.getboostr.portal.database.customization.branding_text;

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
            " LEFT JOIN text.customBrandingTextUserRestrictionEntity userRestriction" +
            " WHERE (" +
            "   (orgAcctRestriction != NULL AND orgAcctRestriction.orgAccountId = :orgAcctId)" +
            "   OR" +
            "   (userRestriction != NULL AND userRestriction.userId = :userId)" +
            " )"
    )
    Page<CustomBrandingTextEntity> findAccessibleCustomBrandingTexts(@Param("orgAcctId") UUID orgAcctId, @Param("userId") UUID userId, Pageable pageable);

}
