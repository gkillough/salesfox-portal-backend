package ai.salesfox.portal.integration.scribeless.workflow;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.integration.scribeless.service.campaign.CampaignService;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignCreationRequestModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignDeleteResponseModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignResponseModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignUpdateRequestModel;
import ai.salesfox.integration.scribeless.service.on_demand.OnDemandService;
import ai.salesfox.integration.scribeless.service.on_demand.model.OnDemandResponseModel;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.GiftRepository;
import ai.salesfox.portal.database.gift.recipient.GiftRecipientEntity;
import ai.salesfox.portal.integration.scribeless.workflow.model.CampaignCreationRequestHolder;
import ai.salesfox.portal.integration.scribeless.workflow.model.ScribelessNoteManagerCampaignStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
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

    @Transactional
    public void submitNoteToScribeless(GiftEntity gift) throws SalesfoxException {
        if (null == gift.getGiftNoteDetailEntity()) {
            // This gift does not have a note
            return;
        }

        CampaignCreationRequestHolder requestHolder = campaignRequestModelCreator.createRequestHolder(gift);
        CampaignResponseModel campaign = createCampaign(gift, requestHolder.getCampaignCreationRequestModel());
        String campaignId = campaign.getId();

        Page<GiftRecipientEntity> recipientsPage = requestHolder.getFirstPageOfRecipients();
        do {
            CampaignUpdateRequestModel updateRequestModel = campaignRequestModelCreator.createUpdateRequestModel(recipientsPage);
            addRecipientsToCampaign(gift, campaignId, updateRequestModel);
            recipientsPage = requestHolder.retrieveNextPageOfRecipients(recipientsPage);
        } while (!recipientsPage.isEmpty());

        OnDemandResponseModel printRequestResult = requestImmediatePrint(gift, campaignId);
        trackCampaignStatus(gift, printRequestResult.getCampaignId(), ScribelessNoteManagerCampaignStatus.SUCCESS_SUBMITTED);
    }

    public void deleteNoteCampaignFromScribeless(GiftEntity gift) {
        // FIXME read campaign info from the DB
        String campaignId = "unknown";
        try {
            if (null != campaignId) {
                CampaignDeleteResponseModel deleteResponse = campaignService.delete(campaignId);
                if (deleteResponse.getSuccess()) {
                    // FIXME delete from the DB
                }
            }
        } catch (SalesfoxException e) {
            log.debug("Failed to delete campaign for gift with id: " + gift.getGiftId(), e);
        }
    }

    @Transactional
    public void resendNoteCampaign(GiftEntity gift) throws SalesfoxException {
        deleteNoteCampaignFromScribeless(gift);
        submitNoteToScribeless(gift);
    }

    private CampaignResponseModel createCampaign(GiftEntity gift, CampaignCreationRequestModel requestModel) throws SalesfoxException {
        try {
            return campaignService.create(requestModel);
        } catch (SalesfoxException e) {
            log.debug("Creating Scribeless campaign failed for gift with id: " + gift.getGiftId(), e);
            trackCampaignStatus(gift, null, ScribelessNoteManagerCampaignStatus.FAILURE_CREATION);
            throw e;
        }
    }

    private void addRecipientsToCampaign(GiftEntity gift, String campaignId, CampaignUpdateRequestModel requestModel) throws SalesfoxException {
        try {
            campaignService.addRecipients(campaignId, requestModel);
        } catch (SalesfoxException e) {
            log.debug("Adding Scribeless recipients failed for gift with id: " + gift.getGiftId(), e);
            trackCampaignStatus(gift, null, ScribelessNoteManagerCampaignStatus.FAILURE_ADD_RECIPIENTS);
            throw e;
        }
    }

    private OnDemandResponseModel requestImmediatePrint(GiftEntity gift, String campaignId) throws SalesfoxException {
        try {
            return onDemandService.requestPrint(campaignId);
        } catch (SalesfoxException e) {
            log.debug("Requesting Scribeless print failed for gift with id: " + gift.getGiftId(), e);
            trackCampaignStatus(gift, campaignId, ScribelessNoteManagerCampaignStatus.FAILURE_REQUEST_PRINT);
            throw e;
        }
    }

    private void trackCampaignStatus(GiftEntity gift, String campaignId, ScribelessNoteManagerCampaignStatus campaignStatus) {
        // FIXME find or create database object
    }

}
