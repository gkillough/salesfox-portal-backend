package ai.salesfox.portal.common.service.license;

import ai.salesfox.portal.common.exception.PortalDatabaseIntegrityViolationException;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseEntity;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseRepository;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LicenseSeatManager {
    private final OrganizationAccountLicenseRepository organizationAccountLicenseRepository;

    @Autowired
    public LicenseSeatManager(OrganizationAccountLicenseRepository organizationAccountLicenseRepository) {
        this.organizationAccountLicenseRepository = organizationAccountLicenseRepository;
    }

    public OrganizationAccountLicenseEntity getLicenseForOrganizationAccount(OrganizationAccountEntity organizationAccountEntity) throws PortalDatabaseIntegrityViolationException {
        return getLicenseForOrganizationAccountId(organizationAccountEntity.getOrganizationAccountId());
    }

    public OrganizationAccountLicenseEntity getLicenseForOrganizationAccountId(UUID orgAccountId) throws PortalDatabaseIntegrityViolationException {
        return organizationAccountLicenseRepository.findById(orgAccountId)
                .orElseThrow(() -> new PortalDatabaseIntegrityViolationException(String.format("Missing license for Organization Account Entity with id: [%s]", orgAccountId.toString())));
    }

    // TODO email org account owner when they exceed the "included" user limit

    /**
     * @return the updated LicenseEntity
     */
    public OrganizationAccountLicenseEntity fillSeat(OrganizationAccountLicenseEntity licenseEntity) {
        licenseEntity.setActiveUsers(licenseEntity.getActiveUsers() + 1);
        return organizationAccountLicenseRepository.save(licenseEntity);
    }

    /**
     * @return the updated LicenseEntity
     */
    public OrganizationAccountLicenseEntity vacateSeat(OrganizationAccountLicenseEntity licenseEntity) {
        licenseEntity.setActiveUsers(licenseEntity.getActiveUsers() - 1);
        return organizationAccountLicenseRepository.save(licenseEntity);
    }

}
