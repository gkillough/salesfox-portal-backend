package ai.salesfox.integration.common.http;

public class QueryParamBuilder {
    public static final char QUERY_STRING_INITIALIZER = '?';
    public static final char QUERY_STRING_SEPARATOR = '&';
    public static final char KEY_VALUE_DELIMITER = '=';

    private StringBuilder queryParamBuilder;

    public QueryParamBuilder(CharSequence initialKey, CharSequence initialValue) {
        this.queryParamBuilder = new StringBuilder(initialKey.length() + initialValue.length());
        appendParam(QUERY_STRING_INITIALIZER, initialKey, initialValue);
    }

    public String build() {
        String queryParamString = queryParamBuilder.toString();
        queryParamBuilder = new StringBuilder();
        return queryParamString;
    }

    public void appendAdditionalParam(CharSequence key, CharSequence value) {
        appendParam(QUERY_STRING_SEPARATOR, key, value);
    }

    private void appendParam(char prefix, CharSequence key, CharSequence value) {
        queryParamBuilder.append(prefix);
        queryParamBuilder.append(key);
        queryParamBuilder.append(KEY_VALUE_DELIMITER);
        queryParamBuilder.append(value);
    }

}
