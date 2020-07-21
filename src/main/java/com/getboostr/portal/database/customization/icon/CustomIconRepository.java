package com.getboostr.portal.database.customization.icon;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface CustomIconRepository extends JpaRepository<CustomIconEntity, UUID> {
    Page<CustomIconEntity> findAllByOrganizationAccountId(UUID organizationAccountId, Pageable pageable);

    @Query("SELECT icon" +
            " FROM CustomIconEntity icon" +
            " RIGHT JOIN icon.customIconOwnerEntity owner" +
            " WHERE owner.userId = :userId"
    )
    Page<CustomIconEntity> findAllByOwningUserId(@Param("userId") UUID userId, Pageable pageable);

}
