package ai.salesfox.integration.scribeless;

import ai.salesfox.integration.scribeless.model.ApiKeyHolder;
import ai.salesfox.integration.scribeless.util.ScribelessApiUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScribelessApiUtilsTest {
    @Test
    public void test() {
        String apiKey = "my api key";
        Boolean testing = Boolean.TRUE;
        String expectedQueryString = "?api_key=" + apiKey + "&testing=" + testing.toString();

        ApiKeyHolder apiKeyHolder = new ApiKeyHolder(apiKey);
        String resultQueryString = ScribelessApiUtils.createDefaultQueryParams(apiKeyHolder, testing);
        assertEquals(expectedQueryString, resultQueryString);
    }

}
