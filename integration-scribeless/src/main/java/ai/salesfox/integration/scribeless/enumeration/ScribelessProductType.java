package ai.salesfox.integration.scribeless.enumeration;

import lombok.Getter;

public enum ScribelessProductType {
    FULL_SERVICE("Full Service"),
    ON_DEMAND("On Demand");

    @Getter
    private final String text;

    ScribelessProductType(String text) {
        this.text = text;
    }

}
