package ai.salesfox.portal.database.organization.account.address;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface OrganizationAccountAddressRepository extends JpaRepository<OrganizationAccountAddressEntity, UUID> {
}
