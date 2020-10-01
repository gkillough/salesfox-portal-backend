package ai.salesfox.portal.common.service.note;

import ai.salesfox.integration.common.util.SalesfoxEnumUtils;
import ai.salesfox.integration.scribeless.enumeration.ScribelessHandwritingColors;
import ai.salesfox.integration.scribeless.enumeration.ScribelessHandwritingStyles;

import java.util.Arrays;

public class NoteValidationUtils {
    public static final int MAX_MESSAGE_CHARS = 2500;
    public static final int MIN_ALLOWED_FONT_SIZE = 8;
    public static final int MAX_ALLOWED_FONT_SIZE = 24;

    public static final String[] ALLOWED_COLORS = SalesfoxEnumUtils.lowercaseValues(ScribelessHandwritingColors.values());
    public static final String[] ALLOWED_HANDWRITING_STYLES = SalesfoxEnumUtils.lowercaseValues(ScribelessHandwritingStyles.values());

    public static boolean isValidMessageSize(String message) {
        return message.length() <= MAX_MESSAGE_CHARS;
    }

    public static boolean isValidFontSize(Integer fontSize) {
        return MIN_ALLOWED_FONT_SIZE <= fontSize && fontSize <= MAX_ALLOWED_FONT_SIZE;
    }

    public static boolean isValidFontColor(String color) {
        return anyMatch(ALLOWED_COLORS, color);
    }

    public static boolean isValidHandwritingStyle(String handwritingStyle) {
        return anyMatch(ALLOWED_HANDWRITING_STYLES, handwritingStyle);
    }

    private static boolean anyMatch(String[] allowedValues, String strToTest) {
        return Arrays.stream(allowedValues)
                .anyMatch(strToTest::equalsIgnoreCase);
    }

}
