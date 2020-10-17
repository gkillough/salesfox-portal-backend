package ai.salesfox.portal.database.organization.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public interface OrganizationAccountRepository extends JpaRepository<OrganizationAccountEntity, UUID> {
    Optional<OrganizationAccountEntity> findFirstByOrganizationIdAndOrganizationAccountName(UUID organizationId, String organizationAccountName);

}
