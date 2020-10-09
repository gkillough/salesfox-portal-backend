package ai.salesfox.portal.integration.scribeless.workflow;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.integration.scribeless.service.campaign.CampaignService;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignCreationRequestModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignDeleteResponseModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignResponseModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignUpdateRequestModel;
import ai.salesfox.integration.scribeless.service.on_demand.OnDemandService;
import ai.salesfox.integration.scribeless.service.on_demand.model.OnDemandResponseModel;
import ai.salesfox.portal.common.service.email.EmailMessagingService;
import ai.salesfox.portal.common.service.email.PortalEmailException;
import ai.salesfox.portal.common.service.email.model.EmailMessageModel;
import ai.salesfox.portal.common.time.PortalDateTimeUtils;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.GiftRepository;
import ai.salesfox.portal.database.gift.recipient.GiftRecipientEntity;
import ai.salesfox.portal.integration.scribeless.database.GiftScribelessStatusEntity;
import ai.salesfox.portal.integration.scribeless.database.GiftScribelessStatusRepository;
import ai.salesfox.portal.integration.scribeless.workflow.model.CampaignCreationRequestHolder;
import ai.salesfox.portal.integration.scribeless.workflow.model.ScribelessNoteManagerCampaignStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This class is for sending "Solo Notes" (i.e. Notes without a Gift Item).
 */
@Slf4j
@Component
public class ScribelessSoloNoteManager {
    private final GiftRepository giftRepository;
    private final GiftScribelessStatusRepository scribelessStatusRepository;
    private final ScribelessCampaignRequestModelCreator campaignRequestModelCreator;
    private final CampaignService campaignService;
    private final OnDemandService onDemandService;
    private final EmailMessagingService emailMessagingService;

    @Autowired
    public ScribelessSoloNoteManager(GiftRepository giftRepository, GiftScribelessStatusRepository scribelessStatusRepository,
                                     ScribelessCampaignRequestModelCreator campaignRequestModelCreator,
                                     CampaignService campaignService, OnDemandService onDemandService,
                                     EmailMessagingService emailMessagingService) {
        this.giftRepository = giftRepository;
        this.scribelessStatusRepository = scribelessStatusRepository;
        this.campaignRequestModelCreator = campaignRequestModelCreator;
        this.campaignService = campaignService;
        this.onDemandService = onDemandService;
        this.emailMessagingService = emailMessagingService;
    }

    @Transactional
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

        if (null != gift.getGiftItemDetailEntity()) {
            // The note will be fulfilled through one of our partners
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

        requestImmediatePrint(gift, campaignId);
        trackCampaignStatus(gift, campaignId, ScribelessNoteManagerCampaignStatus.SUCCESS_SUBMITTED);
    }

    @Transactional
    public void deleteNoteCampaignFromScribeless(GiftEntity gift) {
        UUID giftId = gift.getGiftId();
        String campaignId = scribelessStatusRepository.findById(giftId)
                .map(GiftScribelessStatusEntity::getCampaignId)
                .orElse(null);

        try {
            if (null != campaignId) {
                CampaignDeleteResponseModel deleteResponse = campaignService.delete(campaignId);
                if (deleteResponse.getSuccess()) {
                    scribelessStatusRepository.deleteById(giftId);
                }
            }
        } catch (SalesfoxException e) {
            log.debug("Failed to delete campaign for gift with id: " + giftId, e);
        }
    }

    @Transactional
    // TODO expose an endpoint that lets users try this if the status was unsuccessful
    public void resendNoteCampaign(GiftEntity gift) throws SalesfoxException {
        boolean hasACampaignAlreadyBeenSubmitted = scribelessStatusRepository.findById(gift.getGiftId())
                .map(GiftScribelessStatusEntity::getStatus)
                .filter(ScribelessNoteManagerCampaignStatus.SUCCESS_SUBMITTED.name()::equals)
                .isPresent();
        if (hasACampaignAlreadyBeenSubmitted) {
            throw new SalesfoxException("Cannot resend a note campaign that has already been successfully submitted");
        }

        // TODO this could be done more efficiently depending on where the previous failure occurred
        deleteNoteCampaignFromScribeless(gift);
        submitNoteToScribeless(gift);
    }

    private CampaignResponseModel createCampaign(GiftEntity gift, CampaignCreationRequestModel requestModel) throws SalesfoxException {
        try {
            return campaignService.create(requestModel);
        } catch (SalesfoxException e) {
            log.debug("Creating Scribeless campaign failed for gift with id: " + gift.getGiftId(), e);
            trackCampaignStatus(gift, null, ScribelessNoteManagerCampaignStatus.FAILURE_CREATION);
            sendFailureEmail(gift, null);
            throw e;
        }
    }

    private void addRecipientsToCampaign(GiftEntity gift, String campaignId, CampaignUpdateRequestModel requestModel) throws SalesfoxException {
        try {
            campaignService.addRecipients(campaignId, requestModel);
        } catch (SalesfoxException e) {
            log.debug("Adding Scribeless recipients failed for gift with id: " + gift.getGiftId(), e);
            updateCampaignErrorStatus(gift, ScribelessNoteManagerCampaignStatus.FAILURE_ADD_RECIPIENTS);
            throw e;
        }
    }

    private OnDemandResponseModel requestImmediatePrint(GiftEntity gift, String campaignId) throws SalesfoxException {
        try {
            return onDemandService.requestPrint(campaignId);
        } catch (SalesfoxException e) {
            log.debug("Requesting Scribeless print failed for gift with id: " + gift.getGiftId(), e);
            updateCampaignErrorStatus(gift, ScribelessNoteManagerCampaignStatus.FAILURE_REQUEST_PRINT);
            throw e;
        }
    }

    private void updateCampaignErrorStatus(GiftEntity gift, ScribelessNoteManagerCampaignStatus campaignStatus) {
        UUID giftId = gift.getGiftId();
        Optional<GiftScribelessStatusEntity> optionalCampaignStatus = scribelessStatusRepository.findById(giftId);
        if (optionalCampaignStatus.isPresent()) {
            GiftScribelessStatusEntity scribelessStatus = optionalCampaignStatus.get();
            scribelessStatus.setStatus(campaignStatus.name());
            scribelessStatus.setDateUpdated(PortalDateTimeUtils.getCurrentDateTime());
            scribelessStatusRepository.save(scribelessStatus);
            sendFailureEmail(gift, scribelessStatus.getCampaignId());
        } else {
            log.error("Could not find Scribeless campaign status where one should exist for gift with id: {}", giftId);
            sendFailureEmail(gift, null);
        }

    }

    private void trackCampaignStatus(GiftEntity gift, String campaignId, ScribelessNoteManagerCampaignStatus campaignStatus) {
        OffsetDateTime currentDateTime = PortalDateTimeUtils.getCurrentDateTime();
        GiftScribelessStatusEntity scribelessStatus = new GiftScribelessStatusEntity(gift.getGiftId(), campaignId, campaignStatus.name(), currentDateTime, currentDateTime);
        scribelessStatusRepository.save(scribelessStatus);
    }

    private void sendFailureEmail(GiftEntity gift, @Nullable String campaignId) {
        String userFriendlyCampaignId = Optional.ofNullable(campaignId).orElse("NONE (The campaign could not be created)");
        try {
            UserEntity requestingUser = gift.getRequestingUserEntity();
            EmailMessageModel failureEmailMessage = new EmailMessageModel(
                    List.of(requestingUser.getEmail()),
                    "[Salesfox] Note Send Failure",
                    "Failed to send note(s)",
                    String.format("Gift ID: %s <br/>Campaign ID: %s <br/>Please login to Salesfox for more information or contact support.", gift.getGiftId(), userFriendlyCampaignId)
            );
            emailMessagingService.sendMessage(failureEmailMessage);
        } catch (PortalEmailException e) {
            log.error("Failed to send 'Note Failure Email'", e);
        }
    }

}
