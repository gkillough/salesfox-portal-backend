package ai.salesfox.portal.common.service.support;

import ai.salesfox.integration.common.util.SalesfoxEnumUtils;
import ai.salesfox.portal.common.enumeration.SupportEmailCategory;

import java.util.Arrays;

public class SupportEmailAddressesValidationUtils {
    public static final String[] ALLOWED_CATEGORIES = SalesfoxEnumUtils.capitalizeValues(SupportEmailCategory.values());

    public static boolean isValidCategory(String category) {
        return anyMatch(ALLOWED_CATEGORIES, category);
    }

    private static boolean anyMatch(String[] allowedValues, String strToTest) {
        return Arrays.asList(allowedValues).contains(strToTest);
    }

}
