package ai.salesfox.integration.common.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

public class SalesfoxEnumUtils {
    public static <T extends Enum<T>> String lowercaseValuesString(Enum<T>[] enumValues) {
        return Arrays.toString(lowercaseValues(enumValues));
    }

    public static <T extends Enum<T>> String[] lowercaseValues(Enum<T>[] enumValues) {
        return Optional.ofNullable(enumValues)
                .stream()
                .flatMap(Arrays::stream)
                .map(Enum::name)
                .map(StringUtils::lowerCase)
                .toArray(String[]::new);
    }

}
