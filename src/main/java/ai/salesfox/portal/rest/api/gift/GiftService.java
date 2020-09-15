package ai.salesfox.portal.rest.api.gift;

import ai.salesfox.portal.common.enumeration.AccessOperation;
import ai.salesfox.portal.common.enumeration.GiftTrackingStatus;
import ai.salesfox.portal.common.time.PortalDateTimeUtils;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.catalogue.item.CatalogueItemRepository;
import ai.salesfox.portal.database.contact.OrganizationAccountContactEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactRepository;
import ai.salesfox.portal.database.customization.branding_text.CustomBrandingTextEntity;
import ai.salesfox.portal.database.customization.branding_text.CustomBrandingTextRepository;
import ai.salesfox.portal.database.customization.branding_text.restriction.CustomBrandingTextOrgAccountRestrictionEntity;
import ai.salesfox.portal.database.customization.branding_text.restriction.CustomBrandingTextUserRestrictionEntity;
import ai.salesfox.portal.database.customization.icon.CustomIconEntity;
import ai.salesfox.portal.database.customization.icon.CustomIconRepository;
import ai.salesfox.portal.database.customization.icon.restriction.CustomIconOrganizationAccountRestrictionEntity;
import ai.salesfox.portal.database.customization.icon.restriction.CustomIconUserRestrictionEntity;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.GiftRepository;
import ai.salesfox.portal.database.gift.customization.GiftCustomIconDetailEntity;
import ai.salesfox.portal.database.gift.customization.GiftCustomIconDetailRepository;
import ai.salesfox.portal.database.gift.customization.GiftCustomTextDetailEntity;
import ai.salesfox.portal.database.gift.customization.GiftCustomTextDetailRepository;
import ai.salesfox.portal.database.gift.item.GiftItemDetailEntity;
import ai.salesfox.portal.database.gift.item.GiftItemDetailRepository;
import ai.salesfox.portal.database.gift.note.GiftNoteDetailEntity;
import ai.salesfox.portal.database.gift.note.GiftNoteDetailRepository;
import ai.salesfox.portal.database.gift.recipient.GiftRecipientEntity;
import ai.salesfox.portal.database.gift.recipient.GiftRecipientRepository;
import ai.salesfox.portal.database.gift.restriction.GiftOrgAccountRestrictionEntity;
import ai.salesfox.portal.database.gift.restriction.GiftOrgAccountRestrictionRepository;
import ai.salesfox.portal.database.gift.restriction.GiftUserRestrictionEntity;
import ai.salesfox.portal.database.gift.restriction.GiftUserRestrictionRepository;
import ai.salesfox.portal.database.gift.tracking.GiftTrackingEntity;
import ai.salesfox.portal.database.gift.tracking.GiftTrackingRepository;
import ai.salesfox.portal.database.note.NoteEntity;
import ai.salesfox.portal.database.note.NoteRepository;
import ai.salesfox.portal.database.note.restriction.NoteOrganizationAccountRestrictionEntity;
import ai.salesfox.portal.database.note.restriction.NoteUserRestrictionEntity;
import ai.salesfox.portal.rest.api.common.page.PageRequestValidationUtils;
import ai.salesfox.portal.rest.api.gift.model.DraftGiftRequestModel;
import ai.salesfox.portal.rest.api.gift.model.GiftResponseModel;
import ai.salesfox.portal.rest.api.gift.model.MultiGiftModel;
import ai.salesfox.portal.rest.api.gift.util.GiftAccessService;
import ai.salesfox.portal.rest.api.gift.util.GiftResponseModelUtils;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class GiftService {
    private final GiftRepository giftRepository;
    private final GiftRecipientRepository giftRecipientRepository;
    private final GiftNoteDetailRepository noteDetailRepository;
    private final GiftItemDetailRepository itemDetailRepository;
    private final GiftCustomIconDetailRepository customIconDetailRepository;
    private final GiftCustomTextDetailRepository giftCustomTextDetailRepository;
    private final GiftTrackingRepository giftTrackingRepository;
    private final GiftOrgAccountRestrictionRepository giftOrgAcctRestrictionRepository;
    private final GiftUserRestrictionRepository giftUserRestrictionRepository;
    private final OrganizationAccountContactRepository contactRepository;
    private final NoteRepository noteRepository;
    private final CatalogueItemRepository catalogueItemRepository;
    private final CustomIconRepository customIconRepository;
    private final CustomBrandingTextRepository customBrandingTextRepository;
    private final GiftAccessService giftAccessService;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public GiftService(
            GiftRepository giftRepository,
            GiftRecipientRepository giftRecipientRepository, GiftNoteDetailRepository noteDetailRepository,
            GiftItemDetailRepository itemDetailRepository,
            GiftCustomIconDetailRepository customIconDetailRepository,
            GiftCustomTextDetailRepository giftCustomTextDetailRepository,
            GiftTrackingRepository giftTrackingRepository,
            GiftOrgAccountRestrictionRepository giftOrgAcctRestrictionRepository,
            GiftUserRestrictionRepository giftUserRestrictionRepository,
            OrganizationAccountContactRepository contactRepository,
            NoteRepository noteRepository,
            CatalogueItemRepository catalogueItemRepository,
            CustomIconRepository customIconRepository,
            CustomBrandingTextRepository customBrandingTextRepository,
            GiftAccessService giftAccessService,
            HttpSafeUserMembershipRetrievalService membershipRetrievalService
    ) {
        this.giftRepository = giftRepository;
        this.giftRecipientRepository = giftRecipientRepository;
        this.noteDetailRepository = noteDetailRepository;
        this.itemDetailRepository = itemDetailRepository;
        this.customIconDetailRepository = customIconDetailRepository;
        this.giftCustomTextDetailRepository = giftCustomTextDetailRepository;
        this.giftTrackingRepository = giftTrackingRepository;
        this.giftOrgAcctRestrictionRepository = giftOrgAcctRestrictionRepository;
        this.giftUserRestrictionRepository = giftUserRestrictionRepository;
        this.contactRepository = contactRepository;
        this.noteRepository = noteRepository;
        this.catalogueItemRepository = catalogueItemRepository;
        this.customIconRepository = customIconRepository;
        this.customBrandingTextRepository = customBrandingTextRepository;
        this.giftAccessService = giftAccessService;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public MultiGiftModel getGifts(Integer pageOffset, Integer pageLimit, String giftStatus) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);
        if (null != giftStatus && !EnumUtils.isValidEnum(GiftTrackingStatus.class, giftStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The status [%s] is invalid. Valid statuses: %s", giftStatus, Arrays.toString(GiftTrackingStatus.values())));
        }

        Page<GiftEntity> accessibleGifts = getAccessibleGifts(pageOffset, pageLimit, giftStatus);
        if (accessibleGifts.isEmpty()) {
            return MultiGiftModel.empty();
        }

        List<GiftResponseModel> responseModels = accessibleGifts
                .stream()
                .map(gift -> GiftResponseModelUtils.convertToResponseModel(gift, gift.getGiftRecipients()))
                .collect(Collectors.toList());
        return new MultiGiftModel(responseModels, accessibleGifts);
    }

    public GiftResponseModel getGift(UUID giftId) {
        GiftEntity foundGift = giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        giftAccessService.validateGiftAccess(foundGift, loggedInUser, AccessOperation.READ);

        return GiftResponseModelUtils.convertToResponseModel(foundGift, foundGift.getGiftRecipients());
    }

    @Transactional
    public GiftResponseModel createDraftGift(DraftGiftRequestModel requestModel) {
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        validateRequestModel(loggedInUser, userMembership, requestModel);

        // FIXME update request/response models for multiple contacts
        GiftEntity giftToSave = new GiftEntity(null, loggedInUser.getUserId());
        GiftEntity savedGift = giftRepository.save(giftToSave);
        saveDetails(savedGift, requestModel);

        OffsetDateTime dateCreated = PortalDateTimeUtils.getCurrentDateTime();
        GiftTrackingEntity giftTrackingToSave = new GiftTrackingEntity(savedGift.getGiftId(), GiftTrackingStatus.DRAFT.name(), loggedInUser.getUserId(), dateCreated, dateCreated);
        GiftTrackingEntity savedGiftTracking = giftTrackingRepository.save(giftTrackingToSave);
        savedGift.setGiftTrackingEntity(savedGiftTracking);

        if (membershipRetrievalService.isAuthenticateUserBasicOrPremiumMember()) {
            GiftUserRestrictionEntity userRestrictionToSave = new GiftUserRestrictionEntity(savedGift.getGiftId(), loggedInUser.getUserId());
            GiftUserRestrictionEntity savedUserRestriction = giftUserRestrictionRepository.save(userRestrictionToSave);
            savedGift.setGiftUserRestrictionEntity(savedUserRestriction);
        } else {
            GiftOrgAccountRestrictionEntity orgAcctRestrictionToSave = new GiftOrgAccountRestrictionEntity(savedGift.getGiftId(), userMembership.getOrganizationAccountId());
            GiftOrgAccountRestrictionEntity savedOrgAcctRestriction = giftOrgAcctRestrictionRepository.save(orgAcctRestrictionToSave);
            savedGift.setGiftOrgAccountRestrictionEntity(savedOrgAcctRestriction);
        }

        savedGift.setRequestingUserEntity(loggedInUser);
        List<OrganizationAccountContactEntity> recipients = contactRepository.findById(requestModel.getContactId())
                .map(List::of)
                .orElseGet(List::of);
        return GiftResponseModelUtils.convertToResponseModel(savedGift, recipients);
    }

    @Transactional
    public void updateDraftGift(UUID giftId, DraftGiftRequestModel requestModel) {
        GiftEntity foundGift = giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        giftAccessService.validateGiftAccess(foundGift, loggedInUser, AccessOperation.INTERACT);

        if (!foundGift.isSubmittable()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot edit a gift that has been submitted");
        }

        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        validateRequestModel(loggedInUser, userMembership, requestModel);

        // TODO update gift recipient multiplicity when appropriate
        contactRepository.findById(requestModel.getContactId())
                .map(List::of)
                .ifPresent(foundGift::setGiftRecipients);
        saveDetails(foundGift, requestModel);

        GiftTrackingEntity giftTrackingToUpdate = foundGift.getGiftTrackingEntity();
        giftTrackingToUpdate.setDateUpdated(PortalDateTimeUtils.getCurrentDateTime());
        giftTrackingRepository.save(giftTrackingToUpdate);
    }

    @Transactional
    public void discardDraftGift(UUID giftId) {
        GiftEntity foundGift = giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        giftAccessService.validateGiftAccess(foundGift, loggedInUser, AccessOperation.INTERACT);

        if (!foundGift.isSubmittable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot discard a gift that has been submitted");
        }

        // Tracking and tracking-details will cascade on delete
        giftRepository.delete(foundGift);
    }

    private void saveDetails(GiftEntity savedGift, DraftGiftRequestModel requestModel) {
        if (requestModel.getContactId() != null) {
            // TODO remove this when multiple contacts are possible
            GiftRecipientEntity recipientToSave = new GiftRecipientEntity(savedGift.getGiftId(), requestModel.getContactId());
            giftRecipientRepository.save(recipientToSave);
        }

        if (requestModel.getNoteId() != null) {
            GiftNoteDetailEntity noteDetailToSave = new GiftNoteDetailEntity(savedGift.getGiftId(), requestModel.getNoteId());
            GiftNoteDetailEntity savedNoteDetail = noteDetailRepository.save(noteDetailToSave);
            savedGift.setGiftNoteDetailEntity(savedNoteDetail);
        }

        if (requestModel.getItemId() != null) {
            GiftItemDetailEntity itemDetailToSave = new GiftItemDetailEntity(savedGift.getGiftId(), requestModel.getItemId());
            GiftItemDetailEntity savedItemDetail = itemDetailRepository.save(itemDetailToSave);
            savedGift.setGiftItemDetailEntity(savedItemDetail);
        }

        if (requestModel.getCustomIconId() != null) {
            GiftCustomIconDetailEntity customIconDetailToSave = new GiftCustomIconDetailEntity(savedGift.getGiftId(), requestModel.getCustomIconId());
            GiftCustomIconDetailEntity savedIconDetail = customIconDetailRepository.save(customIconDetailToSave);
            savedGift.setGiftCustomIconDetailEntity(savedIconDetail);
        }

        if (requestModel.getCustomTextId() != null) {
            GiftCustomTextDetailEntity customTextDetailToSave = new GiftCustomTextDetailEntity(savedGift.getGiftId(), requestModel.getCustomTextId());
            GiftCustomTextDetailEntity savedTextDetail = giftCustomTextDetailRepository.save(customTextDetailToSave);
            savedGift.setGiftCustomTextDetailEntity(savedTextDetail);
        }
    }

    private Page<GiftEntity> getAccessibleGifts(Integer pageOffset, Integer pageLimit, @Nullable String giftStatus) {
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return giftRepository.findAll(pageRequest);
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        return giftRepository.findAccessibleGiftsByStatus(userMembership.getOrganizationAccountId(), loggedInUser.getUserId(), giftStatus, pageRequest);
    }

    // TODO clean this method up after a common restriction interface is implemented
    private void validateRequestModel(UserEntity loggedInUser, MembershipEntity userMembership, DraftGiftRequestModel requestModel) {
        List<String> errors = new ArrayList<>();
        if (requestModel.getContactId() == null) {
            errors.add("The request field 'contactId' is required");
        } else {
            Optional<OrganizationAccountContactEntity> optionalContact = contactRepository.findById(requestModel.getContactId());
            if (optionalContact.isPresent()) {
                giftAccessService.validateUserGiftSendingAccessForContact(loggedInUser, optionalContact.get());
            } else {
                errors.add("The contactId provided is invalid");
            }
        }

        if (requestModel.getNoteId() == null && requestModel.getItemId() == null) {
            errors.add("A gift needs at least one note or one item");
        } else {
            validateNoteAndItem(errors, loggedInUser, userMembership, requestModel);
        }

        if (requestModel.getCustomIconId() != null) {
            Optional<CustomIconEntity> optionalCustomIcon = customIconRepository.findById(requestModel.getCustomIconId());
            if (optionalCustomIcon.isPresent()) {
                CustomIconEntity customIcon = optionalCustomIcon.get();
                validateRestrictedEntity(
                        "customIconId",
                        errors,
                        loggedInUser,
                        userMembership,
                        customIcon.getCustomIconOrganizationAccountRestrictionEntity(),
                        customIcon.getCustomIconUserRestrictionEntity(),
                        CustomIconOrganizationAccountRestrictionEntity::getOrganizationAccountId,
                        CustomIconUserRestrictionEntity::getUserId
                );
            } else {
                errors.add("The customIconId provided is invalid");
            }
        }

        if (requestModel.getCustomTextId() != null) {
            Optional<CustomBrandingTextEntity> optionalCustomBrandingText = customBrandingTextRepository.findById(requestModel.getCustomTextId());
            if (optionalCustomBrandingText.isPresent()) {
                CustomBrandingTextEntity customBrandingText = optionalCustomBrandingText.get();
                validateRestrictedEntity(
                        "customTextId",
                        errors,
                        loggedInUser,
                        userMembership,
                        customBrandingText.getCustomBrandingTextOrgAccountRestrictionEntity(),
                        customBrandingText.getCustomBrandingTextUserRestrictionEntity(),
                        CustomBrandingTextOrgAccountRestrictionEntity::getOrgAccountId,
                        CustomBrandingTextUserRestrictionEntity::getUserId
                );
            } else {
                errors.add("The customTextId provided is invalid");
            }
        }

        if (!errors.isEmpty()) {
            String combinedErrors = String.join(", ", errors);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("There were errors with the request: %s", combinedErrors));
        }
    }

    private void validateNoteAndItem(List<String> errors, UserEntity loggedInUser, MembershipEntity userMembership, DraftGiftRequestModel requestModel) {
        if (requestModel.getNoteId() != null) {
            Optional<NoteEntity> optionalNote = noteRepository.findById(requestModel.getNoteId());
            if (optionalNote.isPresent()) {
                NoteEntity requestedNote = optionalNote.get();
                validateRestrictedEntity(
                        "noteId",
                        errors,
                        loggedInUser,
                        userMembership,
                        requestedNote.getNoteOrganizationAccountRestrictionEntity(),
                        requestedNote.getNoteUserRestrictionEntity(),
                        NoteOrganizationAccountRestrictionEntity::getOrganizationAccountId,
                        NoteUserRestrictionEntity::getUserId
                );
            } else {
                errors.add("The noteId provided is invalid");
            }
        }

        if (requestModel.getItemId() != null) {
            if (!catalogueItemRepository.existsById(requestModel.getItemId())) {
                errors.add("The itemId provided is invalid");
            } else {
                giftAccessService.validateUserInventoryAccess(loggedInUser, requestModel.getItemId());
            }
        }
    }

    private <O, U> void validateRestrictedEntity(String idName, List<String> errors, UserEntity loggedInUser, MembershipEntity userMembership,
                                                 O orgAcctRestriction, U userRestriction, Function<O, UUID> orgAcctIdExtractor, Function<U, UUID> userIdExtractor) {
        if (orgAcctRestriction != null && !orgAcctIdExtractor.apply(orgAcctRestriction).equals(userMembership.getOrganizationAccountId())) {
            errors.add(String.format("The %s provided is not accessible from this organization account", idName));
        } else if (userRestriction != null && !userIdExtractor.apply(userRestriction).equals(loggedInUser.getUserId())) {
            errors.add(String.format("The %s provided is not accessible by the requesting user", idName));
        }
    }

}
