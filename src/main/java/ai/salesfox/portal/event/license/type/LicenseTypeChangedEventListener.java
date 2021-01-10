package ai.salesfox.portal.event.license.type;

import ai.salesfox.portal.common.service.billing.LicenseBillingService;
import ai.salesfox.portal.database.license.LicenseTypeEntity;
import ai.salesfox.portal.database.license.LicenseTypeRepository;
import ai.salesfox.portal.event.DefaultDLQHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class LicenseTypeChangedEventListener {
    private final LicenseTypeRepository licenseTypeRepository;
    private final LicenseBillingService licenseBillingService;
    private final DefaultDLQHandler defaultDLQHandler;

    @Autowired
    public LicenseTypeChangedEventListener(
            LicenseTypeRepository licenseTypeRepository,
            LicenseBillingService licenseBillingService,
            DefaultDLQHandler defaultDLQHandler
    ) {
        this.licenseTypeRepository = licenseTypeRepository;
        this.licenseBillingService = licenseBillingService;
        this.defaultDLQHandler = defaultDLQHandler;
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

    @RabbitListener(queues = LicenseTypeChangedEventQueueConfiguration.LICENSE_TYPE_CHANGED_DLQ)
    public void onLicenseChangedProcessingFailure(Message message) {
        defaultDLQHandler.handleQueuedMessageFailure(LicenseTypeChangedEventQueueConfiguration.LICENSE_TYPE_CHANGED_QUEUE, message);
    }

}
