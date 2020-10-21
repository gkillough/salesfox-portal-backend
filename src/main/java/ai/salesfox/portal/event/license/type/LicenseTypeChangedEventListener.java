package ai.salesfox.portal.event.license.type;

import ai.salesfox.portal.database.license.LicenseTypeEntity;
import ai.salesfox.portal.database.license.LicenseTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class LicenseTypeChangedEventListener implements ApplicationListener<LicenseTypeChangedEvent> {
    private final LicenseTypeRepository licenseTypeRepository;

    @Autowired
    public LicenseTypeChangedEventListener(LicenseTypeRepository licenseTypeRepository) {
        this.licenseTypeRepository = licenseTypeRepository;
    }

    @Override
    public void onApplicationEvent(LicenseTypeChangedEvent event) {
        LicenseTypeEntity previousLicenseType = event.getPreviousLicenseTypeState();

        UUID licenseTypeId = previousLicenseType.getLicenseTypeId();
        Optional<LicenseTypeEntity> optionalLicenseType = licenseTypeRepository.findById(licenseTypeId);
        if (optionalLicenseType.isEmpty()) {
            log.warn("Received a LicenseTypeChangedEvent, but no License Type existed in the database. id=[{}]", licenseTypeId);
            return;
        }

        LicenseTypeEntity updatedLicenseType = optionalLicenseType.get();

        // Monthly cost changed
        if (!updatedLicenseType.getMonthlyCost().equals(previousLicenseType.getMonthlyCost())) {
            // TODO update billing
        }

        // Users included on the license decreased
        if (updatedLicenseType.getUsersIncluded() < previousLicenseType.getUsersIncluded()) {
            // TODO update billing
        }

        // Cost per additional user changed
        if (!updatedLicenseType.getCostPerAdditionalUser().equals(previousLicenseType.getCostPerAdditionalUser())) {
            // TODO update billing
        }
    }

}
