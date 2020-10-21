package ai.salesfox.portal.event.license.organization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class OrganizationAccountLicenseChangedEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public OrganizationAccountLicenseChangedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void fireOrgAccountLicenseChangedEvent(UUID orgAccountId, UUID previousLicenseTypeId, Integer previousActiveUsers, Boolean previousActiveStatus) {
        log.debug("Submitting org account license changed event with orgAcctId=[{}]", orgAccountId);
        applicationEventPublisher.publishEvent(new OrganizationAccountLicenseChangedEvent(this, orgAccountId, previousLicenseTypeId, previousActiveUsers, previousActiveStatus));
    }

}
