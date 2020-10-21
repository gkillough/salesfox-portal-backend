package ai.salesfox.portal.event.license.type;

import ai.salesfox.portal.database.license.LicenseTypeEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.PostUpdate;
import javax.persistence.PreUpdate;

/**
 * This class listens to events on the license type entity.
 * Note: Only database events through the Salesfox Portal application will be captured.
 */
@Slf4j
@Component
public class LicenseTypeDatabaseListener {
    private LicenseTypeEntity preUpdateLicenseType;

    @Autowired
    public LicenseTypeDatabaseListener() {
        this.preUpdateLicenseType = null;
    }

    @PreUpdate
    private void beforeAnyUpdate(LicenseTypeEntity licenseTypeCandidate) {
        log.debug("The [{}] license type is a candidate for update. id=[{}]", licenseTypeCandidate.getName(), licenseTypeCandidate.getLicenseTypeId());
        preUpdateLicenseType = licenseTypeCandidate;
    }

    @PostUpdate
    private void afterAnyUpdate(LicenseTypeEntity updatedLicenseType) {
        if (null == preUpdateLicenseType) {
            log.warn("A license type was updated, but no cached preUpdateLicenseType existed. License Type: name=[{}], id=[{}]", updatedLicenseType.getName(), updatedLicenseType.getLicenseTypeId());
            return;
        } else if (!preUpdateLicenseType.getLicenseTypeId().equals(updatedLicenseType.getLicenseTypeId())) {
            log.warn("A license type was updated, but the cached preUpdateLicenseType did not have the same id. License Type: name=[{}], id=[{}]", updatedLicenseType.getName(), updatedLicenseType.getLicenseTypeId());
            preUpdateLicenseType = null;
            return;
        }

        // Monthly cost changed
        if (!updatedLicenseType.getMonthlyCost().equals(preUpdateLicenseType.getMonthlyCost())) {
            // TODO update billing
        }

        // Users included on the license decreased
        if (updatedLicenseType.getUsersIncluded() < preUpdateLicenseType.getUsersIncluded()) {
            // TODO update billing
        }

        // Cost per additional user changed
        if (!updatedLicenseType.getCostPerAdditionalUser().equals(preUpdateLicenseType.getCostPerAdditionalUser())) {
            // TODO update billing
        }

        log.info("The [{}] license type was updated", updatedLicenseType.getName());
        preUpdateLicenseType = null;
    }

}
