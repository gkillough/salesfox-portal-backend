package ai.salesfox.portal.integration.scribeless.workflow;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.integration.scribeless.service.campaign.CampaignService;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignCreationRequestModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignResponseModel;
import ai.salesfox.integration.scribeless.service.on_demand.OnDemandService;
import ai.salesfox.integration.scribeless.service.on_demand.model.OnDemandResponseModel;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.GiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ScribelessNoteManager {
    private final GiftRepository giftRepository;
    private final ScribelessCampaignRequestModelCreator campaignRequestModelCreator;
    private final CampaignService campaignService;
    private final OnDemandService onDemandService;

    @Autowired
    public ScribelessNoteManager(GiftRepository giftRepository, ScribelessCampaignRequestModelCreator campaignRequestModelCreator, CampaignService campaignService, OnDemandService onDemandService) {
        this.giftRepository = giftRepository;
        this.campaignRequestModelCreator = campaignRequestModelCreator;
        this.campaignService = campaignService;
        this.onDemandService = onDemandService;
    }

    public void submitNoteToScribeless(UUID giftId) throws SalesfoxException {
        GiftEntity foundGift = giftRepository.findById(giftId)
                .orElseThrow(() -> new SalesfoxException(String.format("No gift with id: %s", giftId)));
        submitNoteToScribeless(foundGift);
    }

    public void submitNoteToScribeless(GiftEntity gift) throws SalesfoxException {
        CampaignResponseModel campaign = createCampaign(gift);
        OnDemandResponseModel printRequestResult = requestImmediatePrint(gift.getGiftId(), campaign.getId());
        saveCampaignStatus(gift.getGiftId(), printRequestResult.getCampaignId(), "SUCCESS_SUBMITTED");
    }

    private CampaignResponseModel createCampaign(GiftEntity gift) throws SalesfoxException {
        try {
            CampaignCreationRequestModel requestModel = campaignRequestModelCreator.createRequestModel(gift);
            return campaignService.create(requestModel);
        } catch (SalesfoxException e) {
            saveCampaignStatus(gift.getGiftId(), null, "FAILURE_CREATION");
            throw e;
        }
    }

    private OnDemandResponseModel requestImmediatePrint(UUID giftId, String campaignId) throws SalesfoxException {
        try {
            return onDemandService.requestPrint(campaignId);
        } catch (SalesfoxException e) {
            saveCampaignStatus(giftId, campaignId, "FAILURE_REQUEST_PRINT");
            throw e;
        }
    }

    private void saveCampaignStatus(UUID giftId, String campaignId, String campaignStatus) {
        // FIXME find or create database object
    }

}
