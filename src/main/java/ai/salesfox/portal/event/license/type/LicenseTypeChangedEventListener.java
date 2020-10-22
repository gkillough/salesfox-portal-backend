package ai.salesfox.portal.event.license.type;

import ai.salesfox.portal.common.service.billing.LicenseBillingService;
import ai.salesfox.portal.database.license.LicenseTypeEntity;
import ai.salesfox.portal.database.license.LicenseTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class LicenseTypeChangedEventListener {
    private final LicenseTypeRepository licenseTypeRepository;
    private final LicenseBillingService licenseBillingService;

    @Autowired
    public LicenseTypeChangedEventListener(LicenseTypeRepository licenseTypeRepository, LicenseBillingService licenseBillingService) {
        this.licenseTypeRepository = licenseTypeRepository;
        this.licenseBillingService = licenseBillingService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION, fallbackExecution = true)
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

}
