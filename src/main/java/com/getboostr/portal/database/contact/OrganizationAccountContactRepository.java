package com.getboostr.portal.database.contact;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface OrganizationAccountContactRepository extends JpaRepository<OrganizationAccountContactEntity, UUID> {
    Page<OrganizationAccountContactEntity> findAllByIsActive(boolean isActive, Pageable pageable);

    @Query("SELECT contact" +
            " FROM OrganizationAccountContactEntity contact" +
            " LEFT JOIN contact.contactOrganizationAccountRestrictionEntity orgAcctRestriction" +
            " LEFT JOIN contact.contactUserRestrictionEntity userRestriction" +
            " WHERE contact.isActive = :isActive" +
            " AND (" +
            "   (orgAcctRestriction != NULL AND orgAcctRestriction.organizationAccountId = :orgAcctId)" +
            "   OR" +
            "   (userRestriction != NULL AND userRestriction.userId = :userId)" +
            " )"
    )
    Page<OrganizationAccountContactEntity> findByUserIdAndOrganizationAccountIdAndIsActive(@Param("userId") UUID userId, @Param("orgAcctId") UUID organizationAccountId, @Param("isActive") boolean isActive, Pageable pageable);

}
