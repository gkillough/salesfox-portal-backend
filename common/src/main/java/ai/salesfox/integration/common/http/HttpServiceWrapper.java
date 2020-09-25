package ai.salesfox.integration.common.http;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.integration.common.function.ThrowingBiFunction;
import ai.salesfox.integration.common.function.ThrowingFunction;
import ai.salesfox.integration.common.function.ThrowingSupplier;
import com.google.api.client.http.*;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.lang.reflect.Type;

@AllArgsConstructor
public class HttpServiceWrapper {
    @Getter
    private final String baseUrl;
    @Getter
    private final Gson gson;
    private final HttpRequestFactory httpRequestFactory;

    // =========
    // || GET ||
    // =========

    public <T> T executeGet(String uriSpec, Class<T> responseType) throws SalesfoxException {
        return executeGet(HttpRequestConfig.DEFAULT, uriSpec, responseType);
    }

    public <T> T executeGet(HttpRequestConfig requestConfig, String uriSpec, Class<T> responseType) throws SalesfoxException {
        return executeRequestAndParseResponse(() -> executeGet(requestConfig, uriSpec), responseType);
    }

    public HttpResponse executeGet(String uriSpec) throws SalesfoxException {
        return executeRequest(httpRequestFactory::buildGetRequest, uriSpec);
    }

    public HttpResponse executeGet(HttpRequestConfig requestConfig, String uriSpec) throws SalesfoxException {
        return executeRequest(requestConfig, httpRequestFactory::buildGetRequest, uriSpec);
    }

    // ==========
    // || POST ||
    // ==========

    public <T> T executePost(String uriSpec, Object requestContent, Class<T> responseType) throws SalesfoxException {
        return executePost(HttpRequestConfig.DEFAULT, uriSpec, requestContent, responseType);
    }

    public <T> T executePost(HttpRequestConfig requestConfig, String uriSpec, Object requestContent, Class<T> responseType) throws SalesfoxException {
        return executeRequestAndParseResponse(() -> executePost(requestConfig, uriSpec, requestContent), responseType);
    }

    public HttpResponse executePost(String uriSpec, Object requestContent) throws SalesfoxException {
        return executeContentRequest(httpRequestFactory::buildPostRequest, uriSpec, requestContent);
    }

    public HttpResponse executePost(HttpRequestConfig requestConfig, String uriSpec, Object requestContent) throws SalesfoxException {
        return executeContentRequest(requestConfig, httpRequestFactory::buildPostRequest, uriSpec, requestContent);
    }

    // =========
    // || PUT ||
    // =========

    public <T> T executePut(String uriSpec, Object requestContent, Class<T> responseType) throws SalesfoxException {
        return executePut(HttpRequestConfig.DEFAULT, uriSpec, requestContent, responseType);
    }

    public <T> T executePut(HttpRequestConfig requestConfig, String uriSpec, Object requestContent, Class<T> responseType) throws SalesfoxException {
        return executeRequestAndParseResponse(() -> executePut(requestConfig, uriSpec, requestContent), responseType);
    }

    public HttpResponse executePut(String uriSpec, Object requestContent) throws SalesfoxException {
        return executeContentRequest(httpRequestFactory::buildPutRequest, uriSpec, requestContent);
    }

    public HttpResponse executePut(HttpRequestConfig requestConfig, String uriSpec, Object requestContent) throws SalesfoxException {
        return executeContentRequest(requestConfig, httpRequestFactory::buildPutRequest, uriSpec, requestContent);
    }

    // ============
    // || DELETE ||
    // ============

    public <T> T executeDelete(String uriSpec, Class<T> responseType) throws SalesfoxException {
        return executeDelete(HttpRequestConfig.DEFAULT, uriSpec, responseType);
    }

    public <T> T executeDelete(HttpRequestConfig requestConfig, String uriSpec, Class<T> responseType) throws SalesfoxException {
        return executeRequestAndParseResponse(() -> executeDelete(requestConfig, uriSpec), responseType);
    }

    public HttpResponse executeDelete(String uriSpec) throws SalesfoxException {
        return executeRequest(httpRequestFactory::buildDeleteRequest, uriSpec);
    }

    public HttpResponse executeDelete(HttpRequestConfig requestConfig, String uriSpec) throws SalesfoxException {
        return executeRequest(requestConfig, httpRequestFactory::buildDeleteRequest, uriSpec);
    }

    // ======================
    // || GENERIC REQUESTS ||
    // ======================

    public <T> T executeRequestAndParseResponse(ThrowingSupplier<HttpResponse, SalesfoxException> executeRequestFunction, Class<T> responseType) throws SalesfoxException {
        HttpResponse response = executeRequestFunction.get();
        try {
            return parseResponse(response, responseType);
        } finally {
            disconnectResponse(response);
        }
    }

    public HttpResponse executeContentRequest(ThrowingBiFunction<GenericUrl, HttpContent, HttpRequest, IOException> buildRequest, String uriSpec, Object requestContent) throws SalesfoxException {
        return executeContentRequest(HttpRequestConfig.DEFAULT, buildRequest, uriSpec, requestContent);
    }

    public HttpResponse executeContentRequest(HttpRequestConfig requestConfig, ThrowingBiFunction<GenericUrl, HttpContent, HttpRequest, IOException> buildRequest, String uriSpec, Object requestContent) throws SalesfoxException {
        String jsonContent = toJson(requestContent);
        HttpContent httpContent = ByteArrayContent.fromString(requestConfig.getContentType(), jsonContent);
        return executeRequest(requestConfig, (genericUrl) -> buildRequest.apply(genericUrl, httpContent), uriSpec);
    }

    public HttpResponse executeRequest(ThrowingFunction<GenericUrl, HttpRequest, IOException> buildRequest, String uriSpec) throws SalesfoxException {
        return executeRequest(HttpRequestConfig.DEFAULT, buildRequest, uriSpec);
    }

    public HttpResponse executeRequest(HttpRequestConfig requestConfig, ThrowingFunction<GenericUrl, HttpRequest, IOException> buildRequest, String uriSpec) throws SalesfoxException {
        GenericUrl requestUrl = new GenericUrl(baseUrl + uriSpec);
        try {
            HttpRequest request = buildRequest.apply(requestUrl);
            configureHeaders(requestConfig, request);
            return request.execute();
        } catch (IOException ioException) {
            throw new SalesfoxException(ioException);
        }
    }

    // TODO the methods below this line could be abstracted
    // =======================
    // || RESPONSE HANDLING ||
    // =======================

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

    public <T> T parseResponse(HttpResponse response, Type responseType) throws SalesfoxException {
        try {
            String responseJson = response.parseAsString();
            return gson.fromJson(responseJson, responseType);
        } catch (IOException e) {
            throw new SalesfoxException(e);
        }
    }

    // ============================
    // || ADDITIONAL ABSTRACTION ||
    // ============================

    public String toJson(Object targetObject) {
        return gson.toJson(targetObject);
    }

    private void configureHeaders(HttpRequestConfig requestConfig, HttpRequest httpRequest) {
        httpRequest.setConnectTimeout(requestConfig.getRequestTimeout());
        HttpHeaders headers = httpRequest.getHeaders();
        headers.setAccept(requestConfig.getAccept());
        headers.setContentType(requestConfig.getContentType());
    }

}
