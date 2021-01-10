package ai.salesfox.integration.scribeless.enumeration;

import lombok.Getter;

public enum ScribelessCampaignStatus {
    PENDING("Pending"),
    IN_PROGRESS("In progress"),
    READY_FOR_PRINT_QUEUE("Ready for Print Queue");

    @Getter
    private final String value;

    ScribelessCampaignStatus(String value) {
        this.value = value;
    }

}
