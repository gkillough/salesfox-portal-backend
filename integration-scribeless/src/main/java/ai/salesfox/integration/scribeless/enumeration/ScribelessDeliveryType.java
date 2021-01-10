package ai.salesfox.integration.scribeless.enumeration;

import lombok.Getter;

public enum ScribelessDeliveryType {
    /**
     * Scribeless prints and sends the notes
     */
    SHIP_FOR_ME("Ship for me"),
    /**
     * Scribeless prints the notes and sends them to us (or to our distributor?)
     */
    SHIP_TO_SELF("Ship to self");

    @Getter
    private final String value;

    ScribelessDeliveryType(String value) {
        this.value = value;
    }

}
