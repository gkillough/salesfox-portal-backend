package ai.salesfox.integration.scribeless.util;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.integration.common.http.HttpServiceWrapper;
import ai.salesfox.integration.scribeless.model.ApiKeyHolder;
import ai.salesfox.integration.scribeless.model.ScribelessErrorResponseModel;
import com.google.api.client.http.HttpResponse;

import java.lang.reflect.Type;

public class ScribelessApiUtils {
    /**
     * Creates a string of the format "?api_key={api_key}&testing={testing}"
     */
    public static String createDefaultQueryParams(ApiKeyHolder apiKeyHolder, boolean testing) {
        return String.format("?%s=%s&testing=%b", ApiKeyHolder.PARAM_NAME_API_KEY, apiKeyHolder.getApiKey(), testing);
    }

    public static <T> T parseResponseOrImproveError(HttpServiceWrapper httpServiceWrapper, HttpResponse scribelessResponse, Type responseType) throws SalesfoxException {
        if (scribelessResponse.isSuccessStatusCode()) {
            return httpServiceWrapper.parseResponse(scribelessResponse, responseType);
        }

        ScribelessErrorResponseModel errorModel = httpServiceWrapper.parseResponse(scribelessResponse, ScribelessErrorResponseModel.class);
        if (null != errorModel && !errorModel.getSuccess()) {
            throw new SalesfoxException(String.format("Scribeless API error code=[%s] error message: %s", errorModel.getErrorCode(), errorModel.getErrorMessage()));
        }
        throw new SalesfoxException("Scribeless API error");
    }

}
