package ai.salesfox.integration.scribeless.enumeration;

import org.apache.commons.lang3.StringUtils;

public enum ScribelessHandwritingStyles {
    FOSTER,
    GEORGE,
    NIGHTINGALE,
    STAFFORD,
    TREMBLAY;
//    USTHER

    public String getValue() {
        return StringUtils.capitalize(name().toLowerCase());
    }

}
