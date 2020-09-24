package ai.salesfox.integration.scribeless.service.campaign;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.integration.common.http.HttpServiceWrapper;
import ai.salesfox.integration.scribeless.model.ApiKeyHolder;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignCreationRequestModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignDeleteResponseModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignResponseModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignUpdateRequestModel;
import ai.salesfox.integration.scribeless.util.ScribelessRequestSpecUtils;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CampaignService {
    private final boolean testing;
    private final ApiKeyHolder apiKeyHolder;
    private final HttpServiceWrapper httpServiceWrapper;

    public CampaignService(ApiKeyHolder apiKeyHolder, HttpServiceWrapper httpServiceWrapper) {
        this(false, apiKeyHolder, httpServiceWrapper);
    }

    public CampaignResponseModel create(CampaignCreationRequestModel creationRequestModel) {
        String requestSpec = createRequestSpec("");
        // FIXME implement
        return null;
    }

    public CampaignResponseModel addRecipients(String campaignId, CampaignUpdateRequestModel updateRequestModel) {
        String requestSpec = createRequestSpec(campaignId);
        // FIXME implement
        return null;
    }

    public CampaignResponseModel get(String campaignId) throws SalesfoxException {
        String requestSpec = createRequestSpec(campaignId);
        return httpServiceWrapper.executeGet(requestSpec, CampaignResponseModel.class);
    }

    public CampaignDeleteResponseModel delete(String campaignId) {
        String requestSpec = createRequestSpec(campaignId);
        // FIXME implement
        return null;
    }

    private String createRequestSpec(String campaignId) {
        String defaultQueryParams = ScribelessRequestSpecUtils.createDefaultQueryParams(apiKeyHolder, testing);
        return String.format("/campaign/%s%s", campaignId, defaultQueryParams);
    }

}
