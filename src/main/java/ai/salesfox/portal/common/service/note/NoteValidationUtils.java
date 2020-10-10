package ai.salesfox.portal.common.service.note;

import ai.salesfox.integration.common.util.SalesfoxEnumUtils;
import ai.salesfox.integration.scribeless.enumeration.ScribelessHandwritingColors;
import ai.salesfox.integration.scribeless.enumeration.ScribelessHandwritingSize;
import ai.salesfox.integration.scribeless.enumeration.ScribelessHandwritingStyles;

import java.util.Arrays;

public class NoteValidationUtils {
    public static final int MAX_MESSAGE_CHARS = 2500;

    // TODO add ALLOWED_<THINGS>_STRING constants
    public static final String[] ALLOWED_COLORS = SalesfoxEnumUtils.capitalizeValues(ScribelessHandwritingColors.values());
    public static final String[] ALLOWED_FONT_SIZES = SalesfoxEnumUtils.capitalizeValues(ScribelessHandwritingSize.values());
    public static final String[] ALLOWED_HANDWRITING_STYLES = SalesfoxEnumUtils.capitalizeValues(ScribelessHandwritingStyles.values());

    public static boolean isValidMessageSize(String message) {
        return message.length() <= MAX_MESSAGE_CHARS;
    }

    public static boolean isValidFontSize(String fontSize) {
        return anyMatch(ALLOWED_FONT_SIZES, fontSize);
    }

    public static boolean isValidFontColor(String color) {
        return anyMatch(ALLOWED_COLORS, color);
    }

    public static boolean isValidHandwritingStyle(String handwritingStyle) {
        return anyMatch(ALLOWED_HANDWRITING_STYLES, handwritingStyle);
    }

    private static boolean anyMatch(String[] allowedValues, String strToTest) {
        return Arrays.asList(allowedValues).contains(strToTest);
    }

}
