package ai.salesfox.portal.database.contact;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.Collection;
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

    @Query("SELECT COUNT(contact)" +
            " FROM OrganizationAccountContactEntity contact" +
            " LEFT JOIN contact.contactOrganizationAccountRestrictionEntity orgAcctRestriction" +
            " LEFT JOIN contact.contactUserRestrictionEntity userRestriction" +
            " WHERE contact.isActive = true" +
            " AND contact.contactId IN :contactIds" +
            " AND (" +
            "   (orgAcctRestriction != NULL AND orgAcctRestriction.organizationAccountId = :orgAcctId)" +
            "   OR" +
            "   (userRestriction != NULL AND userRestriction.userId = :userId)" +
            " )"
    )
    Integer countVisibleContactsInContactIdCollection(@Param("userId") UUID userId, @Param("orgAcctId") UUID organizationAccountId, @Param("contactIds") Collection<UUID> contactIds);

    @Query("SELECT COUNT(contact)" +
            " FROM OrganizationAccountContactEntity contact" +
            " INNER JOIN contact.contactProfileEntity profile" +
            " LEFT JOIN contact.contactOrganizationAccountRestrictionEntity orgAcctRestriction" +
            " LEFT JOIN contact.contactUserRestrictionEntity userRestriction" +
            " WHERE contact.isActive = true" +
            " AND contact.contactId IN :contactIds" +
            " AND (" +
            "   (orgAcctRestriction != NULL AND orgAcctRestriction.organizationAccountId = :orgAcctId)" +
            "   OR" +
            "   (userRestriction != NULL AND userRestriction.userId = :userId)" +
            " )" +
            " AND (" +
            "   profile.organizationPointOfContactUserId = NULL OR profile.organizationPointOfContactUserId = :userId" +
            " )"
    )
    Integer countInteractableContactsInContactIdCollection(@Param("userId") UUID userId, @Param("orgAcctId") UUID organizationAccountId, @Param("contactIds") Collection<UUID> contactIds);

}
