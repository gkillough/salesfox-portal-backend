package ai.salesfox.portal.event.license.type;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class LicenseTypeChangedEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public LicenseTypeChangedEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void fireLicenseTypeChangedEvent(UUID licenseTypeId, BigDecimal monthlyCost, Integer usersIncluded, BigDecimal costPerAdditionalUser) {
        log.debug("Submitting license type changed event with licenseTypeId=[{}]", licenseTypeId);
        rabbitTemplate.convertAndSend(
                LicenseTypeChangedEventQueueConfiguration.LICENSE_TYPE_CHANGED_EXCHANGE,
                LicenseTypeChangedEventQueueConfiguration.LICENSE_TYPE_CHANGED_QUEUE,
                new LicenseTypeChangedEvent(licenseTypeId, monthlyCost, usersIncluded, costPerAdditionalUser)
        );
    }

}
