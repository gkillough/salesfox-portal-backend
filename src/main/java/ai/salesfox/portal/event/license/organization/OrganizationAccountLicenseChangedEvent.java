package ai.salesfox.portal.event.license.organization;

import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Getter
public class OrganizationAccountLicenseChangedEvent implements Serializable {
    private final UUID orgAccountId;
    private final UUID previousLicenseTypeId;
    private final Integer previousActiveUsers;
    private final Boolean previousActiveStatus;

    public OrganizationAccountLicenseChangedEvent(UUID orgAccountId, UUID previousLicenseTypeId, Integer previousActiveUsers, Boolean previousActiveStatus) {
        this.orgAccountId = orgAccountId;
        this.previousLicenseTypeId = previousLicenseTypeId;
        this.previousActiveUsers = previousActiveUsers;
        this.previousActiveStatus = previousActiveStatus;
    }

}
