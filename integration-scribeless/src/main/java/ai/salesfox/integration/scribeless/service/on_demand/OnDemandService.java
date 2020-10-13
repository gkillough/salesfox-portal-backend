package ai.salesfox.integration.scribeless.service.on_demand;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.integration.common.http.HttpServiceWrapper;
import ai.salesfox.integration.scribeless.model.ApiKeyHolder;
import ai.salesfox.integration.scribeless.service.on_demand.model.OnDemandRecipientResponseModel;
import ai.salesfox.integration.scribeless.service.on_demand.model.OnDemandResponseModel;
import ai.salesfox.integration.scribeless.util.ScribelessApiUtils;
import com.google.api.client.http.HttpResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OnDemandService {
    private final boolean testing;
    private final ApiKeyHolder apiKeyHolder;
    private final HttpServiceWrapper httpServiceWrapper;

    public OnDemandResponseModel requestPrint(String campaignId) throws SalesfoxException {
        String requestSpec = String.format("/api/campaign/%s/request_print", campaignId);
        requestSpec = appendQueryParams(requestSpec);
        HttpResponse response = httpServiceWrapper.executePut(requestSpec, null);
        return ScribelessApiUtils.parseResponseOrImproveError(httpServiceWrapper, response, OnDemandResponseModel.class);
    }

    public OnDemandRecipientResponseModel requestPrintForRecipient(String campaignId, String recipientId) throws SalesfoxException {
        String requestSpec = String.format("/api/campaign/%s/recipient/%s/request_print", campaignId, recipientId);
        requestSpec = appendQueryParams(requestSpec);
        HttpResponse response = httpServiceWrapper.executePut(requestSpec, null);
        return ScribelessApiUtils.parseResponseOrImproveError(httpServiceWrapper, response, OnDemandRecipientResponseModel.class);
    }

    private String appendQueryParams(String requestSpec) {
        String queryParams = ScribelessApiUtils.createDefaultQueryParams(apiKeyHolder, testing);
        return requestSpec + queryParams;
    }

}
