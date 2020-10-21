package ai.salesfox.portal.event.license.type;

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

    @Autowired
    public LicenseTypeChangedEventListener(LicenseTypeRepository licenseTypeRepository) {
        this.licenseTypeRepository = licenseTypeRepository;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION, fallbackExecution = true)
    public void onLicenseChanged(LicenseTypeChangedEvent event) {
        UUID licenseTypeId = event.getLicenseTypeId();
        Optional<LicenseTypeEntity> optionalLicenseType = licenseTypeRepository.findById(licenseTypeId);
        if (optionalLicenseType.isEmpty()) {
            log.warn("Received a LicenseTypeChangedEvent, but no License Type existed in the database. id=[{}]", licenseTypeId);
            return;
        }

        LicenseTypeEntity updatedLicenseType = optionalLicenseType.get();

        // Monthly cost changed
        if (!updatedLicenseType.getMonthlyCost().equals(event.getPreviousMonthlyCost())) {
            // TODO update billing
        }

        // Users included on the license decreased
        if (updatedLicenseType.getUsersIncluded() < event.getPreviousUsersIncluded()) {
            // TODO update billing
        }

        // Cost per additional user changed
        if (!updatedLicenseType.getCostPerAdditionalUser().equals(event.getPreviousCostPerAdditionalUser())) {
            // TODO update billing
        }
    }

}
