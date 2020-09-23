package ai.salesfox.integration.scribeless.service.campaign;

import ai.salesfox.integration.scribeless.model.ApiKeyHolder;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignCreationRequestModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignResponseModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignUpdateRequestModel;

public class CampaignService {
    private final boolean testing;
    private final ApiKeyHolder apiKeyHolder;

    public CampaignService(boolean testing, ApiKeyHolder apiKeyHolder) {
        this.testing = testing;
        this.apiKeyHolder = apiKeyHolder;
    }

    public CampaignResponseModel create(CampaignCreationRequestModel creationRequestModel) {
        // FIXME implement
        return null;
    }

    public CampaignResponseModel addRecipients(CampaignUpdateRequestModel updateRequestModel) {
        // FIXME implement
        return null;
    }

    public CampaignResponseModel get(String campaignId) {
        // FIXME implement
        return null;
    }

    public void delete(String campaignId) {
        // FIXME implement
        //  the DELETE endpoint does return a response with a boolean "success" param, but it may not need to be consumed
    }

}
