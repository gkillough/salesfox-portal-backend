package ai.salesfox.portal.task;

import ai.salesfox.portal.common.enumeration.GiftTrackingStatus;
import ai.salesfox.portal.common.exception.PortalException;
import ai.salesfox.portal.common.service.contact.ContactInteractionsUtility;
import ai.salesfox.portal.common.service.email.EmailMessagingService;
import ai.salesfox.portal.common.service.email.model.EmailMessageModel;
import ai.salesfox.portal.common.service.gift.GiftItemUtility;
import ai.salesfox.portal.common.service.gift.GiftSubmissionUtility;
import ai.salesfox.portal.common.service.gift.GiftTrackingUtility;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.item.GiftItemDetailEntity;
import ai.salesfox.portal.database.inventory.item.InventoryItemEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
public class ScheduledGiftSubmissionUtility extends GiftSubmissionUtility<PortalException> {
    public static final String FAILURE_MESSAGE_SUBJECT_LINE = "Salesfox - Scheduled Gift Submission Failure";

    private final GiftTrackingUtility giftTrackingUtility;
    private final EmailMessagingService emailMessagingService;

    public ScheduledGiftSubmissionUtility(GiftTrackingUtility giftTrackingUtility, GiftItemUtility giftItemUtility, ContactInteractionsUtility contactInteractionsUtility, EmailMessagingService emailMessagingService) {
        super(giftTrackingUtility, giftItemUtility, contactInteractionsUtility);
        this.giftTrackingUtility = giftTrackingUtility;
        this.emailMessagingService = emailMessagingService;
    }

    @Override
    protected void handleGiftNotSubmittable(GiftEntity foundGift, UserEntity submittingUser) throws PortalException {
        unscheduleGift(foundGift, submittingUser);
        notifyUserOfFailure(foundGift.getGiftId(), submittingUser.getEmail(), String.format("A gift was scheduled to be submitted, but its status [%s] prevented this action.", foundGift.getGiftTrackingEntity().getStatus()));
    }

    @Override
    protected void handleItemMissingFromInventory(GiftEntity foundGift, GiftItemDetailEntity giftItemDetail, UserEntity submittingUser) throws PortalException {
        unscheduleGift(foundGift, submittingUser);
        notifyUserOfFailure(foundGift.getGiftId(), submittingUser.getEmail(), "A gift was scheduled to be submitted, but the gift-item was unavailable in the account inventory.");
    }

    @Override
    protected void handleItemOutOfStock(GiftEntity foundGift, InventoryItemEntity inventoryItemForGift, UserEntity submittingUser) throws PortalException {
        unscheduleGift(foundGift, submittingUser);
        notifyUserOfFailure(foundGift.getGiftId(), submittingUser.getEmail(), "A gift was scheduled to be submitted, but the gift-item was out of stock.");
    }

    private void notifyUserOfFailure(UUID giftId, String userEmail, String failureMessage) throws PortalException {
        EmailMessageModel giftSubmissionFailureMessage = new EmailMessageModel(List.of(userEmail), FAILURE_MESSAGE_SUBJECT_LINE, String.format("Gift ID: %s", giftId.toString()), failureMessage);
        emailMessagingService.sendMessage(giftSubmissionFailureMessage);
    }

    private void unscheduleGift(GiftEntity giftEntity, UserEntity submittingUser) {
        giftTrackingUtility.updateGiftTrackingInfo(giftEntity, submittingUser, GiftTrackingStatus.SCHEDULED_SUBMISSION_FAILED);
    }

}
