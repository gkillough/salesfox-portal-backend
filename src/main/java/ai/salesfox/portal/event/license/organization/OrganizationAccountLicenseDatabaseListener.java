package ai.salesfox.portal.event.license.organization;

import ai.salesfox.portal.common.service.email.EmailMessagingService;
import ai.salesfox.portal.common.service.email.PortalEmailException;
import ai.salesfox.portal.common.service.email.model.EmailMessageModel;
import ai.salesfox.portal.database.license.LicenseTypeEntity;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseEntity;
import ai.salesfox.portal.database.organization.OrganizationEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.PostUpdate;
import java.util.Arrays;

/**
 * This class listens to events on the organization account license entity.
 * Note: Only database events through the Salesfox Portal application will be captured.
 */
@Slf4j
public class OrganizationAccountLicenseDatabaseListener {
    // FIXME add an endpoint and service for this
    private static final String[] ADMIN_NOTIFICATION_EMAIL_ADDRESSES = {
            "sales@salesfox.ai",
            "john.simion@salesfox.ai"
    };

    private final EmailMessagingService emailMessagingService;

    @Autowired
    public OrganizationAccountLicenseDatabaseListener(EmailMessagingService emailMessagingService) {
        this.emailMessagingService = emailMessagingService;
    }

    @PostUpdate
    private void afterAnyUpdate(OrganizationAccountLicenseEntity updatedOrgAcctLicense) {
        LicenseTypeEntity licenseType = updatedOrgAcctLicense.getLicenseTypeEntity();

        // Notify Salesfox Admin
        notifyAdminOfLicenseSeatUsed(updatedOrgAcctLicense, licenseType);

        // TODO compare billing information with what's on Stripe for this org account and update it (if necessary)

        // Number of active users has changed and billing update is required

        // Active status changed
    }

    private void notifyAdminOfLicenseSeatUsed(OrganizationAccountLicenseEntity updatedOrgAcctLicense, LicenseTypeEntity licenseType) {
        OrganizationAccountEntity orgAcct = updatedOrgAcctLicense.getOrganizationAccountEntity();
        OrganizationEntity org = orgAcct.getOrganizationEntity();

        EmailMessageModel message = new EmailMessageModel(
                Arrays.asList(ADMIN_NOTIFICATION_EMAIL_ADDRESSES),
                "[Salesfox Portal] New User Added To License",
                String.format("A new user was added to the organization account [%s > %s]", org.getOrganizationName(), orgAcct.getOrganizationAccountName()),
                String.format(
                        "Organization Account ID: %s<br />" +
                                "License Type: %s<br />" +
                                "License Type Included Users: %d<br />" +
                                "Organization Account Current Total Users: %d",
                        orgAcct.getOrganizationAccountId(), licenseType.getName(), licenseType.getUsersIncluded(), updatedOrgAcctLicense.getActiveUsers())
        );
        try {
            emailMessagingService.sendMessage(message);
        } catch (PortalEmailException e) {
            log.warn("Failed to notify Salesfox Admins of license change: {}", e.getMessage());
            log.debug("Send email failure", e);
        }
    }

}
