package com.usepipeline.portal.common;

import com.usepipeline.portal.common.model.PortalAddressModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class FieldValidationUtils {
    // https://emailregex.com/
    private static final String VALID_EMAIL_PATTERN = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

    public static boolean isValidEmailAddress(String emailAddress, boolean allowBlank) {
        return isValidBlank(emailAddress, allowBlank) || emailAddress.matches(VALID_EMAIL_PATTERN);
    }

    public static boolean isValidNumber(String number, boolean allowBlank) {
        return isValidBlank(number, allowBlank) || NumberUtils.isDigits(number);
    }

    public static boolean isValidAddress(PortalAddressModel addressModel, boolean allowBlank) {
        return isValidUSState(addressModel.getState(), allowBlank) && isValidUSZipCode(addressModel.getZipCode(), allowBlank);
    }

    public static boolean isValidUSState(String state, boolean allowBlank) {
        String statePattern = "^[A-Za-z]{2}$";
        return isValidBlank(state, allowBlank) || state.matches(statePattern);
    }

    public static boolean isValidUSZipCode(String zipCode, boolean allowBlank) {
        String zipCodePattern = "^[0-9]{5}(?:-[0-9]{4})?$";
        return isValidBlank(zipCode, allowBlank) || zipCode.matches(zipCodePattern);
    }

    private static boolean isValidBlank(String str, boolean allowBlank) {
        return allowBlank && StringUtils.isBlank(str);
    }

}
