package ai.salesfox.portal.rest.api.gift.util;

import ai.salesfox.portal.common.service.contact.ContactInteractionsService;
import ai.salesfox.portal.common.service.gift.GiftItemService;
import ai.salesfox.portal.common.service.gift.GiftSubmissionUtility;
import ai.salesfox.portal.common.service.gift.GiftTrackingService;
import ai.salesfox.portal.common.service.license.UserLicenseLimitManager;
import ai.salesfox.portal.common.service.note.NoteCreditAvailabilityService;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.account.repository.UserAddressRepository;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.item.GiftItemDetailEntity;
import ai.salesfox.portal.database.gift.recipient.GiftRecipientRepository;
import ai.salesfox.portal.database.inventory.item.InventoryItemEntity;
import ai.salesfox.portal.database.note.credit.NoteCreditsEntity;
import ai.salesfox.portal.database.note.credit.NoteCreditsRepository;
import ai.salesfox.portal.event.gift.GiftSubmittedEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class EndpointGiftSubmissionService extends GiftSubmissionUtility<ResponseStatusException> {
    public static final String FAILURE_MESSAGE_LICENSE_LIMIT_FORMAT_STRING = "Submitting this gift would cause you to exceed the allowed number of %s for this organization account's license.";

    @Autowired
    public EndpointGiftSubmissionService(
            GiftTrackingService giftTrackingService,
            GiftItemService giftItemService,
            UserLicenseLimitManager userLicenseLimitManager,
            GiftRecipientRepository giftRecipientRepository,
            NoteCreditsRepository noteCreditsRepository,
            NoteCreditAvailabilityService noteCreditAvailabilityService,
            ContactInteractionsService contactInteractionsService,
            GiftSubmittedEventPublisher giftSubmittedEventPublisher,
            UserAddressRepository userAddressRepository
    ) {
        super(giftTrackingService, giftItemService, userLicenseLimitManager, giftRecipientRepository, noteCreditsRepository, noteCreditAvailabilityService, contactInteractionsService, giftSubmittedEventPublisher, userAddressRepository);
    }

    @Override
    protected void handleNoRecipients(GiftEntity foundGift, UserEntity submittingUser) throws ResponseStatusException {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This gift has no recipients");
    }

    @Override
    protected void handleGiftNotSubmittable(GiftEntity foundGift, UserEntity submittingUser) throws ResponseStatusException {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This gift has already been submitted");
    }

    @Override
    protected void handleRecipientLimitExceeded(GiftEntity foundGift, UserEntity submittingUser) throws ResponseStatusException {
        throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, String.format(FAILURE_MESSAGE_LICENSE_LIMIT_FORMAT_STRING, "recipients-per-campaign"));
    }

    @Override
    protected void handleCampaignLimitReached(GiftEntity foundGift, UserEntity submittingUser) throws ResponseStatusException {
        throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, String.format(FAILURE_MESSAGE_LICENSE_LIMIT_FORMAT_STRING, "campaigns-per-user"));
    }

    @Override
    protected void handleItemMissingFromInventory(GiftEntity foundGift, GiftItemDetailEntity giftItemDetail, UserEntity submittingUser) throws ResponseStatusException {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested item does not exist in the inventory");
    }

    @Override
    protected void handleItemOutOfStock(GiftEntity foundGift, InventoryItemEntity inventoryItemForGift, UserEntity submittingUser) throws ResponseStatusException {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested item is not in stock");
    }

    @Override
    protected void handleMissingNoteCredits(GiftEntity foundGift, UserEntity submittingUser) throws ResponseStatusException {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough note-credits. Please purchase more.");
    }

    @Override
    protected void handleNotEnoughNoteCredits(GiftEntity foundGift, NoteCreditsEntity noteCredits, UserEntity submittingUser) throws ResponseStatusException {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There were not enough note-credits available to attach a note to the gift");
    }

    @Override
    protected void handleNoReturnAddress(UserEntity submittingUser) throws ResponseStatusException {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Submitting User must have a return address. Please save an address to your user account.");
    }

}
