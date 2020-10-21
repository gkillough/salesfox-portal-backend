package ai.salesfox.portal.event.license.type;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class LicenseTypeChangedEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public LicenseTypeChangedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void fireLicenseTypeChangedEvent(UUID licenseTypeId, BigDecimal monthlyCost, Integer usersIncluded, BigDecimal costPerAdditionalUser) {
        log.debug("Submitting license type changed event with licenseTypeId=[{}]", licenseTypeId);
        applicationEventPublisher.publishEvent(new LicenseTypeChangedEvent(this, licenseTypeId, monthlyCost, usersIncluded, costPerAdditionalUser));
    }

}
