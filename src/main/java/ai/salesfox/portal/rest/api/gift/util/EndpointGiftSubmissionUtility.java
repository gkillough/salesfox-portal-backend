package ai.salesfox.portal.rest.api.gift.util;

import ai.salesfox.portal.common.service.contact.ContactInteractionsUtility;
import ai.salesfox.portal.common.service.gift.GiftItemUtility;
import ai.salesfox.portal.common.service.gift.GiftSubmissionUtility;
import ai.salesfox.portal.common.service.gift.GiftTrackingUtility;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.item.GiftItemDetailEntity;
import ai.salesfox.portal.database.inventory.item.InventoryItemEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EndpointGiftSubmissionUtility extends GiftSubmissionUtility<ResponseStatusException> {
    public EndpointGiftSubmissionUtility(GiftTrackingUtility giftTrackingUtility, GiftItemUtility giftItemUtility, ContactInteractionsUtility contactInteractionsUtility) {
        super(giftTrackingUtility, giftItemUtility, contactInteractionsUtility);
    }

    @Override
    protected void handleGiftNotSubmittable(GiftEntity foundGift, UserEntity submittingUser) throws ResponseStatusException {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This gift has already been submitted");
    }

    @Override
    protected void handleItemMissingFromInventory(GiftEntity foundGift, GiftItemDetailEntity giftItemDetail, UserEntity submittingUser) throws ResponseStatusException {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested item does not exist in the inventory");
    }

    @Override
    protected void handleItemOutOfStock(GiftEntity foundGift, InventoryItemEntity inventoryItemForGift, UserEntity submittingUser) throws ResponseStatusException {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested item is not in stock");
    }

}
