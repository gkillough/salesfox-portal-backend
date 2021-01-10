package ai.salesfox.integration.scribeless.enumeration;

import org.apache.commons.lang3.StringUtils;

public enum ScribelessFontColor {
    BLACK,
    BLUE;

    public String getValue() {
        return StringUtils.capitalize(name().toLowerCase());
    }

}
