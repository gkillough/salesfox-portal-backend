package ai.salesfox.portal.event.license.organization;

import ai.salesfox.portal.database.license.LicenseTypeEntity;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseEntity;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.PostUpdate;
import javax.persistence.PreUpdate;

/**
 * This class listens to events on the organization account license entity.
 * Note: Only database events through the Salesfox Portal application will be captured.
 */
@Slf4j
public class OrganizationAccountLicenseDatabaseListener {
    private OrganizationAccountLicenseEntity preUpdateOrgAcctLicense;

    @PreUpdate
    private void beforeAnyUpdate(OrganizationAccountLicenseEntity orgAcctLicenseCandidate) {
        log.debug("The organization account license with id=[{}] is a candidate for update. orgAccountId=[{}]", orgAcctLicenseCandidate.getLicenseTypeId(), orgAcctLicenseCandidate.getOrganizationAccountId());
        preUpdateOrgAcctLicense = orgAcctLicenseCandidate;
    }

    @PostUpdate
    private void afterAnyUpdate(OrganizationAccountLicenseEntity updatedOrgAcctLicense) {
        if (null == preUpdateOrgAcctLicense) {
            log.warn("An organization account license was updated, but no cached preUpdateOrgAcctLicense existed. licenseTypeId=[{}], orgAccountId=[{}]", updatedOrgAcctLicense.getLicenseTypeId(), updatedOrgAcctLicense.getOrganizationAccountId());
            return;
        } else if (!preUpdateOrgAcctLicense.getOrganizationAccountId().equals(updatedOrgAcctLicense.getOrganizationAccountId())) {
            log.warn("An organization account license was updated, but the cached preUpdateOrgAcctLicense did not have the same id. licenseTypeId=[{}], orgAccountId=[{}]", updatedOrgAcctLicense.getLicenseTypeId(), updatedOrgAcctLicense.getOrganizationAccountId());
            preUpdateOrgAcctLicense = null;
            return;
        }

        LicenseTypeEntity licenseType = updatedOrgAcctLicense.getLicenseTypeEntity();
        Integer usersIncludedWithLicense = licenseType.getUsersIncluded();

        // Number of active users has changed and billing update is required
        if (!updatedOrgAcctLicense.getActiveUsers().equals(preUpdateOrgAcctLicense.getActiveUsers())
                && (preUpdateOrgAcctLicense.getActiveUsers() > usersIncludedWithLicense
                || updatedOrgAcctLicense.getActiveUsers() > usersIncludedWithLicense)) {
            int billableAdditionalUsers = updatedOrgAcctLicense.getActiveUsers() - usersIncludedWithLicense;
            // TODO update billing for new users
        }

        // Active status changed
        if (!preUpdateOrgAcctLicense.getIsActive().equals(updatedOrgAcctLicense.getIsActive())) {
            // TODO Enable/disable billing
        }

        log.info("An organization account license was updated. licenseTypeId=[{}], orgAccountId=[{}]", updatedOrgAcctLicense.getLicenseTypeId(), updatedOrgAcctLicense.getOrganizationAccountId());
        preUpdateOrgAcctLicense = null;
    }

}
