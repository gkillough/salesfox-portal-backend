package ai.salesfox.portal.common.service.license;

import ai.salesfox.portal.database.license.OrganizationAccountLicenseEntity;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LicenseSeatManager {
    private final OrganizationAccountLicenseRepository organizationAccountLicenseRepository;

    @Autowired
    public LicenseSeatManager(OrganizationAccountLicenseRepository organizationAccountLicenseRepository) {
        this.organizationAccountLicenseRepository = organizationAccountLicenseRepository;
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
