package ai.salesfox.portal.event.license.organization;

import ai.salesfox.portal.database.license.OrganizationAccountLicenseEntity;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.PostUpdate;

/**
 * This class listens to events on the organization account license entity.
 * Note: Only database events through the Salesfox Portal application will be captured.
 */
@Slf4j
public class OrganizationAccountLicenseDatabaseListener {
    @PostUpdate
    private void afterAnyUpdate(OrganizationAccountLicenseEntity updatedOrgAcctLicense) {
        // TODO compare billing information with what's on Stripe for this org account and update it (if necessary)

        // Number of active users has changed and billing update is required
        // TODO notify Salesfox Admin

        // Active status changed
    }

}
