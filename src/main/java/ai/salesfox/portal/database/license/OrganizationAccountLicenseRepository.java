package ai.salesfox.portal.database.license;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface OrganizationAccountLicenseRepository extends JpaRepository<OrganizationAccountLicenseEntity, UUID> {
    Slice<OrganizationAccountLicenseEntity> findByLicenseTypeId(UUID licenseTypeId, Pageable pageable);

    boolean existsByLicenseTypeId(UUID licenseTypeId);

    Slice<OrganizationAccountLicenseEntity> findByLicenseTypeIdAndActiveUsersGreaterThan(UUID licenseTypeId, Integer activeUsers, Pageable pageable);

}
