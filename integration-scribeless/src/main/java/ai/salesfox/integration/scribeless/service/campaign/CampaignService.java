package ai.salesfox.integration.scribeless.service.campaign;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.integration.common.http.HttpServiceWrapper;
import ai.salesfox.integration.scribeless.model.ApiKeyHolder;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignCreationRequestModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignDeleteResponseModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignResponseModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignUpdateRequestModel;
import ai.salesfox.integration.scribeless.util.ScribelessApiUtils;
import com.google.api.client.http.HttpResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CampaignService {
    private final boolean testing;
    private final ApiKeyHolder apiKeyHolder;
    private final HttpServiceWrapper httpServiceWrapper;

    public CampaignResponseModel create(CampaignCreationRequestModel creationRequestModel) throws SalesfoxException {
        String requestSpec = createRequestSpec("");
        HttpResponse response = httpServiceWrapper.executePost(requestSpec, creationRequestModel);
        return ScribelessApiUtils.parseResponseOrImproveError(httpServiceWrapper, response, CampaignResponseModel.class);
    }

    public CampaignResponseModel addRecipients(String campaignId, CampaignUpdateRequestModel updateRequestModel) throws SalesfoxException {
        String requestSpec = createRequestSpec(campaignId);
        HttpResponse response = httpServiceWrapper.executePut(requestSpec, updateRequestModel);
        return ScribelessApiUtils.parseResponseOrImproveError(httpServiceWrapper, response, CampaignResponseModel.class);
    }

    public CampaignResponseModel get(String campaignId) throws SalesfoxException {
        String requestSpec = createRequestSpec(campaignId);
        HttpResponse response = httpServiceWrapper.executeGet(requestSpec);
        return ScribelessApiUtils.parseResponseOrImproveError(httpServiceWrapper, response, CampaignResponseModel.class);
    }

    public CampaignDeleteResponseModel delete(String campaignId) throws SalesfoxException {
        String requestSpec = createRequestSpec(campaignId);
        HttpResponse response = httpServiceWrapper.executeDelete(requestSpec);
        return ScribelessApiUtils.parseResponseOrImproveError(httpServiceWrapper, response, CampaignDeleteResponseModel.class);
    }

    private String createRequestSpec(String campaignId) {
        String defaultQueryParams = ScribelessApiUtils.createDefaultQueryParams(apiKeyHolder, testing);
        return String.format("/api/v2/campaign/%s%s", campaignId, defaultQueryParams);
    }

}
