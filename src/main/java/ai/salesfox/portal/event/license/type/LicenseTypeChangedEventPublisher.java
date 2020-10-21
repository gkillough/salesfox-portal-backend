package ai.salesfox.portal.event.license.type;

import ai.salesfox.portal.database.license.LicenseTypeEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LicenseTypeChangedEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public LicenseTypeChangedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void fireLicenseTypeChangedEvent(LicenseTypeEntity previousLicenseTypeEntity) {
        log.debug("Submitting license type changed event with licenseTypeId=[{}]", previousLicenseTypeEntity.getLicenseTypeId());
        applicationEventPublisher.publishEvent(new LicenseTypeChangedEvent(this, previousLicenseTypeEntity));
    }

}
