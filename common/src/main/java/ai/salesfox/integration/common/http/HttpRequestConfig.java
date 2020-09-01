package ai.salesfox.integration.common.http;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HttpRequestConfig {
    public static final HttpRequestConfig DEFAULT = new HttpRequestConfig();

    public static final int REQUEST_TIMEOUT_DEFAULT = 30;
    public static final String CONTENT_TYPE_DEFAULT = "application/json";
    public static final String ACCEPT_DEFAULT = CONTENT_TYPE_DEFAULT;

    private int requestTimeout = REQUEST_TIMEOUT_DEFAULT;
    private String contentType = CONTENT_TYPE_DEFAULT;
    private String accept = ACCEPT_DEFAULT;

    private HttpRequestConfig() {
    }

}
