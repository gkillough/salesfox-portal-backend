package ai.salesfox.integration.scribeless.util;

import ai.salesfox.integration.scribeless.model.ApiKeyHolder;

public class ScribelessRequestSpecUtils {
    /**
     * Creates a string of the format "?api_key={api_key}&testing={testing}"
     */
    public static String createDefaultQueryParams(ApiKeyHolder apiKeyHolder, boolean testing) {
        return String.format("?api_key=%s&testing=%b", apiKeyHolder.getApiKey(), testing);
    }

}
