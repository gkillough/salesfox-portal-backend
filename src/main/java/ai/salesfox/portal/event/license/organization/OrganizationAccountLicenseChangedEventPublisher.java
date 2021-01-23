package ai.salesfox.portal.event.license.organization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class OrganizationAccountLicenseChangedEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public OrganizationAccountLicenseChangedEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void fireOrgAccountLicenseChangedEvent(UUID orgAccountId, UUID previousLicenseTypeId, Integer previousActiveUsers, Boolean previousActiveStatus) {
        log.debug("Submitting org account license changed event with orgAcctId=[{}]", orgAccountId);
        rabbitTemplate.convertAndSend(
                OrganizationAccountLicenseChangedEventQueueConfiguration.LICENSE_CHANGED_EXCHANGE,
                OrganizationAccountLicenseChangedEventQueueConfiguration.LICENSE_CHANGED_QUEUE,
                new OrganizationAccountLicenseChangedEvent(orgAccountId, previousLicenseTypeId, previousActiveUsers, previousActiveStatus)
        );
    }

}
