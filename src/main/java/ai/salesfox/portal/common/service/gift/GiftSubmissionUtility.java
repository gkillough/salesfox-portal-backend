package ai.salesfox.portal.common.service.gift;

import ai.salesfox.portal.common.enumeration.GiftTrackingStatus;
import ai.salesfox.portal.common.enumeration.InteractionClassification;
import ai.salesfox.portal.common.enumeration.InteractionMedium;
import ai.salesfox.portal.common.service.contact.ContactInteractionsUtility;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.item.GiftItemDetailEntity;
import ai.salesfox.portal.database.inventory.item.InventoryItemEntity;
import ai.salesfox.portal.database.inventory.item.InventoryItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public abstract class GiftSubmissionUtility<E extends Throwable> {
    private final GiftTrackingUtility giftTrackingUtility;
    private final GiftItemUtility giftItemUtility;
    private final ContactInteractionsUtility<E> contactInteractionsUtility;

    public GiftSubmissionUtility(GiftTrackingUtility giftTrackingUtility, GiftItemUtility giftItemUtility, ContactInteractionsUtility<E> contactInteractionsUtility, InventoryItemRepository inventoryItemRepository) {
        this.giftItemUtility = giftItemUtility;
        this.contactInteractionsUtility = contactInteractionsUtility;
        this.giftTrackingUtility = giftTrackingUtility;
    }

    public GiftEntity submitGift(GiftEntity foundGift, UserEntity submittingUser) throws E {
        if (!foundGift.isSubmittable()) {
            handleGiftNotSubmittable(foundGift, submittingUser);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This gift has already been submitted");
        }

        MembershipEntity userMembership = submittingUser.getMembershipEntity();
        GiftItemDetailEntity giftItemDetail = foundGift.getGiftItemDetailEntity();
        if (giftItemDetail != null) {
            InventoryItemEntity inventoryItemForGift = giftItemUtility.findInventoryItemForGift(submittingUser, userMembership, giftItemDetail)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested item does not exist in the inventory"));
            giftItemUtility.decrementItemQuantityOrElseThrow(inventoryItemForGift, () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested item is not in stock"));
        }

        giftTrackingUtility.updateGiftTrackingInfo(foundGift, submittingUser, GiftTrackingStatus.SUBMITTED.name());
        contactInteractionsUtility.addContactInteraction(submittingUser, foundGift.getContactId(), InteractionMedium.MAIL, InteractionClassification.OUTGOING, "(Auto-generated) Sent gift/note");
        return foundGift;
    }

    protected abstract void handleGiftNotSubmittable(GiftEntity foundGift, UserEntity submittingUser) throws E;

}
