package ai.salesfox.integration.scribeless.enumeration;

import lombok.Getter;

public enum ScribelessProductType {
    /**
     * The note will be fulfilled by Scribeless.
     */
    FULL_SERVICE("Full Service"),
    /**
     * The note will be fulfilled by a partner.
     */
    ON_DEMAND("On Demand");

    @Getter
    private final String text;

    ScribelessProductType(String text) {
        this.text = text;
    }

}
