package ai.salesfox.portal.common.service.license;

import ai.salesfox.portal.database.license.OrganizationAccountLicenseEntity;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseRepository;
import ai.salesfox.portal.event.license.organization.OrganizationAccountLicenseChangedEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LicenseSeatManager {
    private final OrganizationAccountLicenseRepository organizationAccountLicenseRepository;
    private final OrganizationAccountLicenseChangedEventPublisher orgAccountLicenseChangedEventPublisher;

    @Autowired
    public LicenseSeatManager(OrganizationAccountLicenseRepository organizationAccountLicenseRepository, OrganizationAccountLicenseChangedEventPublisher orgAccountLicenseChangedEventPublisher) {
        this.organizationAccountLicenseRepository = organizationAccountLicenseRepository;
        this.orgAccountLicenseChangedEventPublisher = orgAccountLicenseChangedEventPublisher;
    }

    // TODO abstract these methods

    /**
     *
     */
    public void fillSeat(OrganizationAccountLicenseEntity licenseEntity) {
        int previousActiveUsers = licenseEntity.getActiveUsers();

        licenseEntity.setActiveUsers(licenseEntity.getActiveUsers() + 1);
        organizationAccountLicenseRepository.save(licenseEntity);
        orgAccountLicenseChangedEventPublisher.fireOrgAccountLicenseChangedEvent(licenseEntity.getOrganizationAccountId(), licenseEntity.getLicenseTypeId(), previousActiveUsers, licenseEntity.getIsActive());
    }

    /**
     *
     */
    public void vacateSeat(OrganizationAccountLicenseEntity licenseEntity) {
        int previousActiveUsers = licenseEntity.getActiveUsers();

        licenseEntity.setActiveUsers(licenseEntity.getActiveUsers() - 1);
        organizationAccountLicenseRepository.save(licenseEntity);
        orgAccountLicenseChangedEventPublisher.fireOrgAccountLicenseChangedEvent(licenseEntity.getOrganizationAccountId(), licenseEntity.getLicenseTypeId(), previousActiveUsers, licenseEntity.getIsActive());
    }

}
