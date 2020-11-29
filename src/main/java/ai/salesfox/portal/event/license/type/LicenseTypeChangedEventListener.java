package ai.salesfox.portal.event.license.type;

import ai.salesfox.portal.common.service.billing.LicenseBillingService;
import ai.salesfox.portal.common.service.email.EmailMessagingService;
import ai.salesfox.portal.common.service.email.PortalEmailAddressConfiguration;
import ai.salesfox.portal.common.service.email.model.EmailMessageModel;
import ai.salesfox.portal.database.license.LicenseTypeEntity;
import ai.salesfox.portal.database.license.LicenseTypeRepository;
import ai.salesfox.portal.event.license.organization.OrganizationAccountLicenseChangedEventQueueConfiguration;
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
public class LicenseTypeChangedEventListener {
    private final LicenseTypeRepository licenseTypeRepository;
    private final LicenseBillingService licenseBillingService;
    private final EmailMessagingService emailMessagingService;
    private final PortalEmailAddressConfiguration portalEmailAddressConfiguration;

    @Autowired
    public LicenseTypeChangedEventListener(
            LicenseTypeRepository licenseTypeRepository,
            LicenseBillingService licenseBillingService,
            EmailMessagingService emailMessagingService,
            PortalEmailAddressConfiguration portalEmailAddressConfiguration
    ) {
        this.licenseTypeRepository = licenseTypeRepository;
        this.licenseBillingService = licenseBillingService;
        this.emailMessagingService = emailMessagingService;
        this.portalEmailAddressConfiguration = portalEmailAddressConfiguration;
    }

    @RabbitListener(queues = LicenseTypeChangedEventQueueConfiguration.LICENSE_TYPE_CHANGED_QUEUE)
    public void onLicenseTypeChanged(LicenseTypeChangedEvent event) {
        UUID licenseTypeId = event.getLicenseTypeId();
        Optional<LicenseTypeEntity> optionalLicenseType = licenseTypeRepository.findById(licenseTypeId);
        if (optionalLicenseType.isEmpty()) {
            log.warn("Received a LicenseTypeChangedEvent, but no License Type existed in the database. id=[{}]", licenseTypeId);
            return;
        }

        LicenseTypeEntity updatedLicenseType = optionalLicenseType.get();

        // Monthly cost changed
        if (!updatedLicenseType.getMonthlyCost().equals(event.getPreviousMonthlyCost())) {
            licenseBillingService.updateLicenseMonthlyCost(licenseTypeId, updatedLicenseType.getMonthlyCost());
        }

        // Users included on the license decreased or cost per additional user changed
        if (!updatedLicenseType.getUsersIncluded().equals(event.getPreviousUsersIncluded())
                || !updatedLicenseType.getCostPerAdditionalUser().equals(event.getPreviousCostPerAdditionalUser())) {
            licenseBillingService.updateLicenseUsersIncluded(licenseTypeId, updatedLicenseType.getUsersIncluded(), event.getPreviousUsersIncluded(), updatedLicenseType.getCostPerAdditionalUser(), event.getPreviousCostPerAdditionalUser());
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

}
