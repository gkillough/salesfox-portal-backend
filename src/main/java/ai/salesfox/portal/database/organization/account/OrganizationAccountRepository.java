package ai.salesfox.portal.database.organization.account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public interface OrganizationAccountRepository extends JpaRepository<OrganizationAccountEntity, UUID> {
    Optional<OrganizationAccountEntity> findFirstByOrganizationIdAndOrganizationAccountName(UUID organizationId, String organizationAccountName);

    @Query("SELECT orgAccount" +
            " FROM OrganizationAccountEntity orgAccount" +
            " JOIN orgAccount.organizationEntity org" +
            " WHERE org.organizationName LIKE %:query%" +
            " OR orgAccount.organizationAccountName LIKE %:query%"
    )
    Page<OrganizationAccountEntity> findByQuery(@Param("query") String query, Pageable pageable);

}
