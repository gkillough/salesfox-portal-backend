package ai.salesfox.integration.common.http;

import ai.salesfox.integration.common.exception.PortalException;
import com.google.api.client.http.*;
import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
public class HttpService {
    private final String baseUrl;
    private final HttpRequestFactory httpRequestFactory;

    public String getBaseUrl() {
        return baseUrl;
    }

    public <T> T executeGet(String uriSpec, Class<T> responseType) throws PortalException {
        return executeGet(HttpRequestConfig.DEFAULT, uriSpec, responseType);
    }

    public <T> T executeGet(HttpRequestConfig requestConfig, String uriSpec, Class<T> responseType) throws PortalException {
        HttpResponse response = executeGet(requestConfig, uriSpec);
        try {
            return response.parseAs(responseType);
        } catch (IOException ioException) {
            throw new PortalException(ioException);
        } finally {
            disconnectResponse(response);
        }
    }

    public HttpResponse executeGet(String uriSpec) throws PortalException {
        return executeGet(HttpRequestConfig.DEFAULT, uriSpec);
    }

    public HttpResponse executeGet(HttpRequestConfig requestConfig, String uriSpec) throws PortalException {
        GenericUrl urlToGet = new GenericUrl(baseUrl + uriSpec);
        try {
            HttpRequest getRequest = httpRequestFactory.buildGetRequest(urlToGet);
            configureHeaders(requestConfig, getRequest);
            return getRequest.execute();
        } catch (IOException ioException) {
            throw new PortalException(ioException);
        }
    }

    public void disconnectResponse(HttpResponse response) throws PortalException {
        if (response == null) {
            return;
        }
        try {
            response.disconnect();
        } catch (IOException e) {
            throw new PortalException(e);
        }
    }

    private void configureHeaders(HttpRequestConfig requestConfig, HttpRequest httpRequest) {
        httpRequest.setConnectTimeout(requestConfig.getRequestTimeout());
        HttpHeaders headers = httpRequest.getHeaders();
        headers.setAccept(requestConfig.getAccept());
        headers.setContentType(requestConfig.getContentType());
    }

}
