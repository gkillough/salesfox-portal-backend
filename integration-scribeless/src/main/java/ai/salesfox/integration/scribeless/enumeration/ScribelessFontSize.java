package ai.salesfox.integration.scribeless.enumeration;

import org.apache.commons.lang3.StringUtils;

public enum ScribelessFontSize {
    SMALL,
    MEDIUM,
    LARGE;

    public String getValue() {
        return StringUtils.capitalize(name().toLowerCase());
    }

}
