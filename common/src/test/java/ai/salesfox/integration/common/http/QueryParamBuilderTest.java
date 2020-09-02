package ai.salesfox.integration.common.http;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QueryParamBuilderTest {
    @Test
    public void buildTest() {
        String key1 = "key1";
        String value1 = "value1";
        String key2 = "key2";
        String value2 = "value2";

        QueryParamBuilder queryParamBuilder = new QueryParamBuilder(key1, value1);
        queryParamBuilder.appendAdditionalParam(key2, value2);

        String expectedQueryString = String.format("?%s=%s&%s=%s", key1, value1, key2, value2);
        assertEquals(expectedQueryString, queryParamBuilder.build());
    }

    @Test
    public void noTrailingAmperstandTest() {
        String key1 = "key1";
        String value1 = "value1";

        QueryParamBuilder queryParamBuilder = new QueryParamBuilder(key1, value1);

        String queryString = queryParamBuilder.build();
        assertFalse(StringUtils.endsWith(queryString, "&"), "Expected the constructed query string to not have a trailing '&'");
    }

}
