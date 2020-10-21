package ai.salesfox.portal.event.license.type;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.util.UUID;

public class LicenseTypeChangedEvent extends ApplicationEvent {
    @Getter
    private final UUID licenseTypeId;
    @Getter
    private final BigDecimal previousMonthlyCost;
    @Getter
    private final Integer previousUsersIncluded;
    @Getter
    private final BigDecimal previousCostPerAdditionalUser;

    public LicenseTypeChangedEvent(Object source, UUID licenseTypeId, BigDecimal previousMonthlyCost, Integer previousUsersIncluded, BigDecimal previousCostPerAdditionalUser) {
        super(source);
        this.licenseTypeId = licenseTypeId;
        this.previousMonthlyCost = previousMonthlyCost;
        this.previousUsersIncluded = previousUsersIncluded;
        this.previousCostPerAdditionalUser = previousCostPerAdditionalUser;
    }

}
