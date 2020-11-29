package ai.salesfox.portal.event.license.organization;

import ai.salesfox.portal.common.service.billing.LicenseBillingService;
import ai.salesfox.portal.common.service.email.EmailMessagingService;
import ai.salesfox.portal.common.service.email.PortalEmailAddressConfiguration;
import ai.salesfox.portal.common.service.email.PortalEmailException;
import ai.salesfox.portal.common.service.email.model.EmailMessageModel;
import ai.salesfox.portal.database.license.LicenseTypeEntity;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseEntity;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseRepository;
import ai.salesfox.portal.database.organization.OrganizationEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
// TODO consider splitting this event handling up
public class OrganizationAccountLicenseChangedEventListener {
    private final OrganizationAccountLicenseRepository organizationAccountLicenseRepository;
    private final LicenseBillingService licenseBillingService;
    private final EmailMessagingService emailMessagingService;
    private final PortalEmailAddressConfiguration portalEmailAddressConfiguration;

    @Autowired
    public OrganizationAccountLicenseChangedEventListener(OrganizationAccountLicenseRepository organizationAccountLicenseRepository, LicenseBillingService licenseBillingService, EmailMessagingService emailMessagingService, PortalEmailAddressConfiguration portalEmailAddressConfiguration) {
        this.organizationAccountLicenseRepository = organizationAccountLicenseRepository;
        this.licenseBillingService = licenseBillingService;
        this.emailMessagingService = emailMessagingService;
        this.portalEmailAddressConfiguration = portalEmailAddressConfiguration;
    }

    @RabbitListener(queues = OrganizationAccountLicenseChangedEventQueueConfiguration.LICENSE_CHANGED_QUEUE)
    public void onOrgAccountLicenseChanged(OrganizationAccountLicenseChangedEvent event) {
        UUID orgAccountLicenseId = event.getOrgAccountId();
        Optional<OrganizationAccountLicenseEntity> optionalOrgAccountLicense = organizationAccountLicenseRepository.findById(orgAccountLicenseId);
        if (optionalOrgAccountLicense.isEmpty()) {
            log.warn("Received a OrganizationAccountLicenseChangedEvent, but no Org Account License existed in the database. id=[{}]", orgAccountLicenseId);
            return;
        }

        OrganizationAccountLicenseEntity updatedOrgAccountLicense = optionalOrgAccountLicense.get();
        LicenseTypeEntity licenseType = updatedOrgAccountLicense.getLicenseTypeEntity();

        int previousActiveUsers = event.getPreviousActiveUsers();
        int newActiveUsers = updatedOrgAccountLicense.getActiveUsers();

        // License Type changed
        if (!event.getPreviousLicenseTypeId().equals(licenseType.getLicenseTypeId())) {
            licenseBillingService.updateOrgAccountLicenseType(updatedOrgAccountLicense, licenseType);
        }

        // Number of active users has changed and billing update is required
        if (previousActiveUsers != newActiveUsers) {
            licenseBillingService.updateOrgAccountLicenseActiveUsers(updatedOrgAccountLicense, newActiveUsers);

            // Notify Salesfox Admin
            if (previousActiveUsers < newActiveUsers) {
                notifyAdminOfLicenseSeatUsed(updatedOrgAccountLicense, licenseType);
            }
        }

        // Active status changed
        if (!event.getPreviousActiveStatus().equals(updatedOrgAccountLicense.getIsActive())) {
            if (updatedOrgAccountLicense.getIsActive()) {
                licenseBillingService.activateOrgAccountLicense(updatedOrgAccountLicense);
            } else {
                licenseBillingService.deactivateOrgAccountLicense(updatedOrgAccountLicense);
            }
        }
    }

    // TODO abstract DLQ handling
    @RabbitListener(queues = OrganizationAccountLicenseChangedEventQueueConfiguration.LICENSE_CHANGED_DLQ)
    public void onLicenseChangedProcessingFailure(Message message) {
        try {
            log.error(String.format("Could not send message: %s", message.toString()));
            String primaryMessage = String.format("Queued Message Handling Failed: %s <br/>", message.toString());

            String portalSupportEmail = portalEmailAddressConfiguration.getSupportEmailAddress();
            EmailMessageModel errorEmail = new EmailMessageModel(List.of(portalSupportEmail), "[Salesfox] Distribution Failure", "Distribution Failure", primaryMessage);
            emailMessagingService.sendMessage(errorEmail);
        } catch (Exception e) {
            log.error("Handler failed for {}: {}", OrganizationAccountLicenseChangedEventQueueConfiguration.LICENSE_CHANGED_DLQ, e.getMessage());
        }
    }

    private void notifyAdminOfLicenseSeatUsed(OrganizationAccountLicenseEntity updatedOrgAcctLicense, LicenseTypeEntity licenseType) {
        OrganizationAccountEntity orgAcct = updatedOrgAcctLicense.getOrganizationAccountEntity();
        OrganizationEntity org = orgAcct.getOrganizationEntity();

        String portalSupportEmail = portalEmailAddressConfiguration.getSupportEmailAddress();
        EmailMessageModel message = new EmailMessageModel(
                List.of(portalSupportEmail),
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
