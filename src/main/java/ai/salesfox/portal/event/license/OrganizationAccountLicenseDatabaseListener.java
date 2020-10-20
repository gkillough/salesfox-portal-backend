package ai.salesfox.portal.event.license;

import ai.salesfox.portal.database.license.OrganizationAccountLicenseEntity;
import ai.salesfox.portal.database.organization.OrganizationEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.PostUpdate;

/**
 * This class listens to events on the organization account license entity.
 * Note: Only database events through this application will be captured.
 */
@Slf4j
public class OrganizationAccountLicenseDatabaseListener {
    @PostUpdate
    private void afterAnyUpdate(OrganizationAccountLicenseEntity updatedOrgAcctLicense) {
        OrganizationAccountEntity orgAcct = updatedOrgAcctLicense.getOrganizationAccountEntity();
        OrganizationEntity org = orgAcct.getOrganizationEntity();
        log.info("The license for the [{}] organization [{}] account was updated", org.getOrganizationName(), orgAcct.getOrganizationAccountName());
        // FIXME handle updates
    }

}
