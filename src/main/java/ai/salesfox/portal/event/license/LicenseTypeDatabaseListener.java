package ai.salesfox.portal.event.license;

import ai.salesfox.portal.database.license.LicenseTypeEntity;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.PostUpdate;

/**
 * This class listens to events on the license type entity.
 * Note: Only database events through this application will be captured.
 */
@Slf4j
public class LicenseTypeDatabaseListener {
    @PostUpdate
    private void afterAnyUpdate(LicenseTypeEntity updatedLicenseType) {
        log.info("The [{}] license type was updated", updatedLicenseType.getName());
        // FIXME handle updates
    }

}
