package ai.salesfox.portal.event.license.type;

import ai.salesfox.portal.database.license.LicenseTypeEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class LicenseTypeChangedEvent extends ApplicationEvent {
    @Getter
    private final LicenseTypeEntity previousLicenseTypeState;

    public LicenseTypeChangedEvent(Object source, LicenseTypeEntity previousLicenseTypeState) {
        super(source);
        this.previousLicenseTypeState = previousLicenseTypeState;
    }

}
