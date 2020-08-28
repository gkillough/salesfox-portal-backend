package ai.salesfox.portal.common.service.gift;

import ai.salesfox.portal.common.enumeration.GiftTrackingStatus;
import ai.salesfox.portal.common.enumeration.InteractionClassification;
import ai.salesfox.portal.common.enumeration.InteractionMedium;
import ai.salesfox.portal.common.service.contact.ContactInteractionsService;
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
    private final GiftTrackingService giftTrackingService;
    private final GiftItemService giftItemService;
    private final ContactInteractionsService contactInteractionsService;

    public GiftSubmissionUtility(GiftTrackingService giftTrackingService, GiftItemService giftItemService, ContactInteractionsService contactInteractionsService) {
        this.giftItemService = giftItemService;
        this.contactInteractionsService = contactInteractionsService;
        this.giftTrackingService = giftTrackingService;
    }

    @Transactional
    public Optional<GiftEntity> submitGift(GiftEntity gift, UserEntity submittingUser) throws E {
        if (!gift.isSubmittable()) {
            handleGiftNotSubmittable(gift, submittingUser);
            return Optional.empty();
        }

        MembershipEntity userMembership = submittingUser.getMembershipEntity();
        GiftItemDetailEntity giftItemDetail = gift.getGiftItemDetailEntity();
        if (giftItemDetail != null) {
            Optional<InventoryItemEntity> optionalGiftItem = giftItemService.findInventoryItemForGift(submittingUser, userMembership, giftItemDetail);
            if (optionalGiftItem.isPresent()) {
                // TODO decrement available notes
                giftItemService.decrementItemQuantityOrElse(optionalGiftItem.get(), giftItem -> handleItemOutOfStock(gift, giftItem, submittingUser));
            } else {
                handleItemMissingFromInventory(gift, giftItemDetail, submittingUser);
                return Optional.empty();
            }
        }

        giftTrackingService.updateGiftTrackingInfo(gift, submittingUser, GiftTrackingStatus.SUBMITTED.name());
        contactInteractionsService.addContactInteraction(submittingUser, gift.getContactId(), InteractionMedium.MAIL, InteractionClassification.OUTGOING, "(Auto-generated) Sent gift/note")
                .ifPresentOrElse(ignored -> {
                }, () -> log.warn("Failed to add auto-generated gift submission interaction to contact with id: [{}]", gift.getContactId()));
        return Optional.of(gift);
    }

    protected abstract void handleGiftNotSubmittable(GiftEntity foundGift, UserEntity submittingUser) throws E;

    protected abstract void handleItemMissingFromInventory(GiftEntity foundGift, GiftItemDetailEntity giftItemDetail, UserEntity submittingUser) throws E;

    protected abstract void handleItemOutOfStock(GiftEntity foundGift, InventoryItemEntity inventoryItemForGift, UserEntity submittingUser) throws E;

}
