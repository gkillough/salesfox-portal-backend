package ai.salesfox.portal.event.license.organization;

import ai.salesfox.portal.common.service.email.EmailMessagingService;
import ai.salesfox.portal.common.service.email.PortalEmailException;
import ai.salesfox.portal.common.service.email.model.EmailMessageModel;
import ai.salesfox.portal.database.license.LicenseTypeEntity;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseEntity;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseRepository;
import ai.salesfox.portal.database.organization.OrganizationEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * This class listens to events on the organization account license entity.
 * Note: Only database events through the Salesfox Portal application will be captured.
 */
@Slf4j
// TODO consider splitting this event handling up
public class OrganizationAccountLicenseChangedListener {
    // FIXME add an endpoint and service for this
    private static final String[] ADMIN_NOTIFICATION_EMAIL_ADDRESSES = {
            "sales@salesfox.ai",
            "john.simion@salesfox.ai"
    };

    private final OrganizationAccountLicenseRepository organizationAccountLicenseRepository;
    private final EmailMessagingService emailMessagingService;

    @Autowired
    public OrganizationAccountLicenseChangedListener(OrganizationAccountLicenseRepository organizationAccountLicenseRepository, EmailMessagingService emailMessagingService) {
        this.organizationAccountLicenseRepository = organizationAccountLicenseRepository;
        this.emailMessagingService = emailMessagingService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION, fallbackExecution = true)
    private void onOrgAccountLicenseChanged(OrganizationAccountLicenseChangedEvent event) {
        UUID orgAccountLicenseId = event.getOrgAccountId();
        Optional<OrganizationAccountLicenseEntity> optionalOrgAccountLicense = organizationAccountLicenseRepository.findById(orgAccountLicenseId);
        if (optionalOrgAccountLicense.isEmpty()) {
            log.warn("Received a OrganizationAccountLicenseChangedEvent, but no Org Account License existed in the database. id=[{}]", orgAccountLicenseId);
            return;
        }

        OrganizationAccountLicenseEntity updatedOrgAccountLicense = optionalOrgAccountLicense.get();
        LicenseTypeEntity licenseType = updatedOrgAccountLicense.getLicenseTypeEntity();

        // Notify Salesfox Admin
        notifyAdminOfLicenseSeatUsed(updatedOrgAccountLicense, licenseType);

        // TODO compare billing information with what's on Stripe for this org account and update it (if necessary)

        // Number of active users has changed and billing update is required

        // Active status changed
        // TODO add the event publisher to the place where this field can be changed
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
