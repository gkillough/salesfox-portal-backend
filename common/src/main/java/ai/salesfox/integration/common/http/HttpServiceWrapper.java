package ai.salesfox.integration.common.http;

import ai.salesfox.integration.common.exception.SalesfoxException;
import com.google.api.client.http.*;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.lang.reflect.Type;

@AllArgsConstructor
public class HttpServiceWrapper {
    private final String baseUrl;
    private final Gson gson;
    private final HttpRequestFactory httpRequestFactory;

    public String getBaseUrl() {
        return baseUrl;
    }

    public <T> T executeGet(String uriSpec, Class<T> responseType) throws SalesfoxException {
        return executeGet(HttpRequestConfig.DEFAULT, uriSpec, responseType);
    }

    public <T> T executeGet(HttpRequestConfig requestConfig, String uriSpec, Class<T> responseType) throws SalesfoxException {
        HttpResponse response = executeGet(requestConfig, uriSpec);
        try {
            return parseResponse(response, responseType);
        } catch (IOException ioException) {
            throw new SalesfoxException(ioException);
        } finally {
            disconnectResponse(response);
        }
    }

    public HttpResponse executeGet(String uriSpec) throws SalesfoxException {
        return executeGet(HttpRequestConfig.DEFAULT, uriSpec);
    }

    public HttpResponse executeGet(HttpRequestConfig requestConfig, String uriSpec) throws SalesfoxException {
        GenericUrl urlToGet = new GenericUrl(baseUrl + uriSpec);
        try {
            HttpRequest getRequest = httpRequestFactory.buildGetRequest(urlToGet);
            configureHeaders(requestConfig, getRequest);
            return getRequest.execute();
        } catch (IOException ioException) {
            throw new SalesfoxException(ioException);
        }
    }

    public void disconnectResponse(HttpResponse response) throws SalesfoxException {
        if (response == null) {
            return;
        }
        try {
            response.disconnect();
        } catch (IOException e) {
            throw new SalesfoxException(e);
        }
    }

    public <T> T parseResponse(HttpResponse response, Type responseType) throws IOException {
        String responseJson = response.parseAsString();
        return gson.fromJson(responseJson, responseType);
    }

    private void configureHeaders(HttpRequestConfig requestConfig, HttpRequest httpRequest) {
        httpRequest.setConnectTimeout(requestConfig.getRequestTimeout());
        HttpHeaders headers = httpRequest.getHeaders();
        headers.setAccept(requestConfig.getAccept());
        headers.setContentType(requestConfig.getContentType());
    }

}
