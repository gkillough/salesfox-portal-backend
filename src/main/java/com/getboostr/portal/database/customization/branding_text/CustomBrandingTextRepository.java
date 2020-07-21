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
    Page<CustomBrandingTextEntity> findAllByOrganizationAccountId(UUID organizationAccountId, Pageable pageable);

    @Query("SELECT text" +
            " FROM CustomBrandingTextEntity text" +
            " RIGHT JOIN text.customBrandingTextOwnerEntity owner" +
            " WHERE owner.userId = :userId"
    )
    Page<CustomBrandingTextEntity> findAllByOwningUserId(@Param("userId") UUID userId, Pageable pageable);

}
