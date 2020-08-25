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
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
public abstract class GiftSubmissionUtility<E extends Throwable> {
    private final GiftTrackingUtility giftTrackingUtility;
    private final GiftItemUtility giftItemUtility;
    private final ContactInteractionsUtility contactInteractionsUtility;

    public GiftSubmissionUtility(GiftTrackingUtility giftTrackingUtility, GiftItemUtility giftItemUtility, ContactInteractionsUtility contactInteractionsUtility) {
        this.giftItemUtility = giftItemUtility;
        this.contactInteractionsUtility = contactInteractionsUtility;
        this.giftTrackingUtility = giftTrackingUtility;
    }

    @Transactional
    public Optional<GiftEntity> submitGift(GiftEntity foundGift, UserEntity submittingUser) throws E {
        if (!foundGift.isSubmittable()) {
            handleGiftNotSubmittable(foundGift, submittingUser);
            return Optional.empty();
            // throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This gift has already been submitted");
        }

        MembershipEntity userMembership = submittingUser.getMembershipEntity();
        GiftItemDetailEntity giftItemDetail = foundGift.getGiftItemDetailEntity();
        if (giftItemDetail != null) {
            Optional<InventoryItemEntity> optionalGiftItem = giftItemUtility.findInventoryItemForGift(submittingUser, userMembership, giftItemDetail);
            if (optionalGiftItem.isPresent()) {
                // new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested item is not in stock")
                giftItemUtility.decrementItemQuantityOrElse(optionalGiftItem.get(), giftItem -> handleItemOutOfStock(foundGift, giftItem, submittingUser));
            } else {
                handleItemMissingFromInventory(foundGift, giftItemDetail, submittingUser);
                return Optional.empty();
                // throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested item does not exist in the inventory");
            }

        }

        giftTrackingUtility.updateGiftTrackingInfo(foundGift, submittingUser, GiftTrackingStatus.SUBMITTED.name());
        contactInteractionsUtility.addContactInteraction(submittingUser, foundGift.getContactId(), InteractionMedium.MAIL, InteractionClassification.OUTGOING, "(Auto-generated) Sent gift/note")
                .ifPresentOrElse(ignored -> {
                }, () -> log.warn("Failed to add auto-generated gift submission interaction to contact with id: [{}]", foundGift.getContactId()));
        return Optional.of(foundGift);
    }

    protected abstract void handleGiftNotSubmittable(GiftEntity foundGift, UserEntity submittingUser) throws E;

    protected abstract void handleItemMissingFromInventory(GiftEntity foundGift, GiftItemDetailEntity giftItemDetail, UserEntity submittingUser) throws E;

    protected abstract void handleItemOutOfStock(GiftEntity foundGift, InventoryItemEntity inventoryItemForGift, UserEntity submittingUser) throws E;

}
