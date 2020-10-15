package ai.salesfox.portal.database.license;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface OrganizationAccountLicenseRepository extends JpaRepository<OrganizationAccountLicenseEntity, UUID> {
    boolean existsByLicenseTypeId(UUID licenseTypeId);

}
