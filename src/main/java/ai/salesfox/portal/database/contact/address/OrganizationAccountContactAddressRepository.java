package ai.salesfox.portal.database.contact.address;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface OrganizationAccountContactAddressRepository extends JpaRepository<OrganizationAccountContactAddressEntity, UUID> {
}
