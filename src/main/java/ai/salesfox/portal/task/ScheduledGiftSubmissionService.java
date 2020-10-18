package ai.salesfox.portal.task;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.portal.common.enumeration.GiftTrackingStatus;
import ai.salesfox.portal.common.service.contact.ContactInteractionsService;
import ai.salesfox.portal.common.service.email.EmailMessagingService;
import ai.salesfox.portal.common.service.email.model.EmailMessageModel;
import ai.salesfox.portal.common.service.gift.GiftItemService;
import ai.salesfox.portal.common.service.gift.GiftSubmissionUtility;
import ai.salesfox.portal.common.service.gift.GiftTrackingService;
import ai.salesfox.portal.common.service.license.UserLicenseLimitManager;
import ai.salesfox.portal.common.service.note.NoteCreditAvailabilityService;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.item.GiftItemDetailEntity;
import ai.salesfox.portal.database.gift.recipient.GiftRecipientRepository;
import ai.salesfox.portal.database.inventory.item.InventoryItemEntity;
import ai.salesfox.portal.database.note.credit.NoteCreditsEntity;
import ai.salesfox.portal.database.note.credit.NoteCreditsRepository;
import ai.salesfox.portal.event.GiftSubmittedEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class ScheduledGiftSubmissionService extends GiftSubmissionUtility<SalesfoxException> {
    public static final String FAILURE_MESSAGE_SUBJECT_LINE = "Salesfox - Scheduled Gift Submission Failure";
    public static final String FAILURE_MESSAGE_LICENSE_LIMIT_FORMAT_STRING = "A gift was scheduled to be submitted, but it would have exceeded the allowed number of %s for this organization account's license.";

    private final GiftTrackingService giftTrackingService;
    private final EmailMessagingService emailMessagingService;

    @Autowired
    public ScheduledGiftSubmissionService(
            GiftTrackingService giftTrackingService,
            GiftItemService giftItemService,
            UserLicenseLimitManager userLicenseLimitManager,
            GiftRecipientRepository giftRecipientRepository,
            NoteCreditsRepository noteCreditsRepository,
            NoteCreditAvailabilityService noteCreditAvailabilityService,
            ContactInteractionsService contactInteractionsService,
            EmailMessagingService emailMessagingService,
            GiftSubmittedEventPublisher giftSubmittedEventPublisher
    ) {
        super(giftTrackingService, giftItemService, userLicenseLimitManager, giftRecipientRepository, noteCreditsRepository, noteCreditAvailabilityService, contactInteractionsService, giftSubmittedEventPublisher);
        this.giftTrackingService = giftTrackingService;
        this.emailMessagingService = emailMessagingService;
    }

    @Override
    protected void handleNoRecipients(GiftEntity foundGift, UserEntity submittingUser) throws SalesfoxException {
        unscheduleGift(foundGift, submittingUser);
        notifyUserOfFailure(foundGift.getGiftId(), submittingUser.getEmail(), "A gift was scheduled to be submitted, but it had no recipients.");
    }

    @Override
    protected void handleGiftNotSubmittable(GiftEntity foundGift, UserEntity submittingUser) throws SalesfoxException {
        unscheduleGift(foundGift, submittingUser);
        notifyUserOfFailure(foundGift.getGiftId(), submittingUser.getEmail(), String.format("A gift was scheduled to be submitted, but its status [%s] prevented this action.", foundGift.getGiftTrackingEntity().getStatus()));
    }

    @Override
    protected void handleRecipientLimitExceeded(GiftEntity foundGift, UserEntity submittingUser) throws SalesfoxException {
        unscheduleGift(foundGift, submittingUser);
        notifyUserOfFailure(foundGift.getGiftId(), submittingUser.getEmail(), String.format(FAILURE_MESSAGE_LICENSE_LIMIT_FORMAT_STRING, "recipients-per-campaign"));
    }

    @Override
    protected void handleCampaignLimitReached(GiftEntity foundGift, UserEntity submittingUser) throws SalesfoxException {
        unscheduleGift(foundGift, submittingUser);
        notifyUserOfFailure(foundGift.getGiftId(), submittingUser.getEmail(), String.format(FAILURE_MESSAGE_LICENSE_LIMIT_FORMAT_STRING, "campaigns-per-user"));
    }

    @Override
    protected void handleItemMissingFromInventory(GiftEntity foundGift, GiftItemDetailEntity giftItemDetail, UserEntity submittingUser) throws SalesfoxException {
        unscheduleGift(foundGift, submittingUser);
        notifyUserOfFailure(foundGift.getGiftId(), submittingUser.getEmail(), "A gift was scheduled to be submitted, but the gift-item was unavailable in the account inventory.");
    }

    @Override
    protected void handleItemOutOfStock(GiftEntity foundGift, InventoryItemEntity inventoryItemForGift, UserEntity submittingUser) throws SalesfoxException {
        unscheduleGift(foundGift, submittingUser);
        notifyUserOfFailure(foundGift.getGiftId(), submittingUser.getEmail(), "A gift was scheduled to be submitted, but the gift-item was out of stock.");
    }

    @Override
    protected void handleMissingNoteCredits(GiftEntity foundGift, UserEntity submittingUser) throws SalesfoxException {
        unscheduleGift(foundGift, submittingUser);
        notifyUserOfFailure(foundGift.getGiftId(), submittingUser.getEmail(), "A gift was scheduled to be submitted, but the there was a problem tracking note-credits.");
    }

    @Override
    protected void handleNotEnoughNoteCredits(GiftEntity foundGift, NoteCreditsEntity noteCredits, UserEntity submittingUser) throws SalesfoxException {
        unscheduleGift(foundGift, submittingUser);
        notifyUserOfFailure(foundGift.getGiftId(), submittingUser.getEmail(), "A gift was scheduled to be submitted, but the there were not enough note-credits.");
    }

    private void notifyUserOfFailure(UUID giftId, String userEmail, String failureMessage) throws SalesfoxException {
        EmailMessageModel giftSubmissionFailureMessage = new EmailMessageModel(List.of(userEmail), FAILURE_MESSAGE_SUBJECT_LINE, String.format("Gift ID: %s", giftId.toString()), failureMessage);
        emailMessagingService.sendMessage(giftSubmissionFailureMessage);
    }

    private void unscheduleGift(GiftEntity giftEntity, UserEntity submittingUser) {
        giftTrackingService.updateGiftTrackingInfo(giftEntity, submittingUser, GiftTrackingStatus.SCHEDULED_SUBMISSION_FAILED);
    }

}
