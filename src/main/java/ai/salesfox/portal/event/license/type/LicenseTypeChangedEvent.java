package ai.salesfox.portal.event.license.type;

import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class LicenseTypeChangedEvent implements Serializable {
    private final UUID licenseTypeId;
    private final BigDecimal previousMonthlyCost;
    private final Integer previousUsersIncluded;
    private final BigDecimal previousCostPerAdditionalUser;

    public LicenseTypeChangedEvent(UUID licenseTypeId, BigDecimal previousMonthlyCost, Integer previousUsersIncluded, BigDecimal previousCostPerAdditionalUser) {
        this.licenseTypeId = licenseTypeId;
        this.previousMonthlyCost = previousMonthlyCost;
        this.previousUsersIncluded = previousUsersIncluded;
        this.previousCostPerAdditionalUser = previousCostPerAdditionalUser;
    }

}
