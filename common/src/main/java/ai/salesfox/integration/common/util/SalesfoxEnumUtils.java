package ai.salesfox.integration.common.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

// TODO write unit tests for these
public class SalesfoxEnumUtils {
    public static <T extends Enum<T>> String lowercaseValuesString(Enum<T>[] enumValues) {
        return Arrays.toString(lowercaseValues(enumValues));
    }

    public static <T extends Enum<T>> String[] lowercaseValues(Enum<T>[] enumValues) {
        return transformValues(enumValues, StringUtils::lowerCase);
    }

    public static <T extends Enum<T>> String capitalizeValuesString(Enum<T>[] enumValues) {
        return Arrays.toString(capitalizeValues(enumValues));
    }

    public static <T extends Enum<T>> String[] capitalizeValues(Enum<T>[] enumValues) {
        return transformValues(enumValues, str -> StringUtils.capitalize(StringUtils.lowerCase(str)));
    }

    private static <T extends Enum<T>> String[] transformValues(Enum<T>[] enumValues, Function<String, String> transformer) {
        return Optional.ofNullable(enumValues)
                .stream()
                .flatMap(Arrays::stream)
                .map(Enum::name)
                .map(transformer)
                .toArray(String[]::new);
    }

}
