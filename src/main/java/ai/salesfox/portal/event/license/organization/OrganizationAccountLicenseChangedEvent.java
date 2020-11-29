package ai.salesfox.portal.event.license.organization;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.io.Serializable;
import java.util.UUID;

public class OrganizationAccountLicenseChangedEvent extends ApplicationEvent implements Serializable {
    @Getter
    private final UUID orgAccountId;
    @Getter
    private final UUID previousLicenseTypeId;
    @Getter
    private final Integer previousActiveUsers;
    @Getter
    private final Boolean previousActiveStatus;

    public OrganizationAccountLicenseChangedEvent(Object source, UUID orgAccountId, UUID previousLicenseTypeId, Integer previousActiveUsers, Boolean previousActiveStatus) {
        super(source);
        this.orgAccountId = orgAccountId;
        this.previousLicenseTypeId = previousLicenseTypeId;
        this.previousActiveUsers = previousActiveUsers;
        this.previousActiveStatus = previousActiveStatus;
    }

}
