package ai.salesfox.integration.scribeless.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ApiKeyHolder {
    public static final String PARAM_NAME_API_KEY = "api_key";

    @Getter
    private final CharSequence apiKey;

}
