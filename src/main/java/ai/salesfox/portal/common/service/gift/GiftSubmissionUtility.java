package ai.salesfox.portal.common.service.gift;

import ai.salesfox.portal.common.enumeration.GiftTrackingStatus;
import ai.salesfox.portal.common.enumeration.InteractionClassification;
import ai.salesfox.portal.common.enumeration.InteractionMedium;
import ai.salesfox.portal.common.service.contact.ContactInteractionsService;
import ai.salesfox.portal.common.service.note.NoteCreditAvailabilityService;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.item.GiftItemDetailEntity;
import ai.salesfox.portal.database.gift.note.GiftNoteDetailEntity;
import ai.salesfox.portal.database.gift.recipient.GiftRecipientEntity;
import ai.salesfox.portal.database.gift.recipient.GiftRecipientRepository;
import ai.salesfox.portal.database.inventory.item.InventoryItemEntity;
import ai.salesfox.portal.database.note.credit.NoteCreditsEntity;
import ai.salesfox.portal.database.note.credit.NoteCreditsRepository;
import ai.salesfox.portal.event.GiftSubmittedEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public abstract class GiftSubmissionUtility<E extends Throwable> {
    private final GiftTrackingService giftTrackingService;
    private final GiftItemService giftItemService;
    private final GiftRecipientRepository giftRecipientRepository;
    private final NoteCreditsRepository noteCreditsRepository;
    private final NoteCreditAvailabilityService noteCreditAvailabilityService;
    private final ContactInteractionsService contactInteractionsService;
    private final GiftSubmittedEventPublisher giftSubmittedEventPublisher;

    public GiftSubmissionUtility(GiftTrackingService giftTrackingService, GiftItemService giftItemService,
                                 GiftRecipientRepository giftRecipientRepository, NoteCreditsRepository noteCreditsRepository, NoteCreditAvailabilityService noteCreditAvailabilityService,
                                 ContactInteractionsService contactInteractionsService, GiftSubmittedEventPublisher giftSubmittedEventPublisher) {
        this.giftItemService = giftItemService;
        this.giftRecipientRepository = giftRecipientRepository;
        this.noteCreditsRepository = noteCreditsRepository;
        this.noteCreditAvailabilityService = noteCreditAvailabilityService;
        this.contactInteractionsService = contactInteractionsService;
        this.giftTrackingService = giftTrackingService;
        this.giftSubmittedEventPublisher = giftSubmittedEventPublisher;
    }

    @Transactional
    public Optional<GiftEntity> submitGift(GiftEntity gift, UserEntity submittingUser) throws E {
        if (!gift.isSubmittable()) {
            handleGiftNotSubmittable(gift, submittingUser);
            return Optional.empty();
        }

        Set<UUID> giftRecipientIds = giftRecipientRepository.findByGiftId(gift.getGiftId())
                .stream()
                .map(GiftRecipientEntity::getContactId)
                .collect(Collectors.toSet());
        if (giftRecipientIds.isEmpty()) {
            handleNoRecipients(gift, submittingUser);
            return Optional.empty();
        }

        Integer recipientCount = giftRecipientIds.size();

        MembershipEntity userMembership = submittingUser.getMembershipEntity();
        GiftItemDetailEntity giftItemDetail = gift.getGiftItemDetailEntity();
        if (null != giftItemDetail) {
            Optional<InventoryItemEntity> optionalGiftItem = giftItemService.findInventoryItemForGift(submittingUser, userMembership, giftItemDetail);
            if (optionalGiftItem.isPresent()) {
                boolean inventorySuccess = giftItemService.decrementItemQuantityOrElse(optionalGiftItem.get(), recipientCount.longValue(), giftItem -> handleItemOutOfStock(gift, giftItem, submittingUser));
                if (!inventorySuccess) {
                    return Optional.empty();
                }
            } else {
                handleItemMissingFromInventory(gift, giftItemDetail, submittingUser);
                return Optional.empty();
            }
        }

        GiftNoteDetailEntity giftNoteDetail = gift.getGiftNoteDetailEntity();
        if (null != giftNoteDetail) {
            Optional<NoteCreditsEntity> optionalNoteCredits = noteCreditsRepository.findAccessibleNoteCredits(userMembership.getOrganizationAccountId(), submittingUser.getUserId());
            if (optionalNoteCredits.isPresent()) {
                boolean noteCreditSuccess = noteCreditAvailabilityService.decrementNoteCreditsOrElse(optionalNoteCredits.get(), recipientCount, noteCredits -> handleNotEnoughNoteCredits(gift, noteCredits, submittingUser));
                if (!noteCreditSuccess) {
                    return Optional.empty();
                }
            } else {
                handleMissingNoteCredits(gift, submittingUser);
                return Optional.empty();
            }
        }

        // TODO consider moving this to the event handler
        giftTrackingService.updateGiftTrackingInfo(gift, submittingUser, GiftTrackingStatus.SUBMITTED.name());
        contactInteractionsService.addContactInteractions(submittingUser, giftRecipientIds, InteractionMedium.MAIL, InteractionClassification.OUTGOING, "(Auto-generated) Sent gift/note");

        giftSubmittedEventPublisher.fireGiftSubmittedEvent(gift.getGiftId(), submittingUser.getUserId());

        return Optional.of(gift);
    }

    protected abstract void handleNoRecipients(GiftEntity foundGift, UserEntity submittingUser) throws E;

    protected abstract void handleGiftNotSubmittable(GiftEntity foundGift, UserEntity submittingUser) throws E;

    protected abstract void handleItemMissingFromInventory(GiftEntity foundGift, GiftItemDetailEntity giftItemDetail, UserEntity submittingUser) throws E;

    protected abstract void handleItemOutOfStock(GiftEntity foundGift, InventoryItemEntity inventoryItemForGift, UserEntity submittingUser) throws E;

    protected abstract void handleMissingNoteCredits(GiftEntity foundGift, UserEntity submittingUser) throws E;

    protected abstract void handleNotEnoughNoteCredits(GiftEntity foundGift, NoteCreditsEntity noteCredits, UserEntity submittingUser) throws E;

}
