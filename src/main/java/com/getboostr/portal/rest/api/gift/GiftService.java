package com.getboostr.portal.rest.api.gift;

import com.getboostr.portal.common.enumeration.AccessOperation;
import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.catalogue.item.CatalogueItemRepository;
import com.getboostr.portal.database.contact.OrganizationAccountContactEntity;
import com.getboostr.portal.database.contact.OrganizationAccountContactRepository;
import com.getboostr.portal.database.customization.branding_text.CustomBrandingTextEntity;
import com.getboostr.portal.database.customization.branding_text.CustomBrandingTextRepository;
import com.getboostr.portal.database.customization.icon.CustomIconEntity;
import com.getboostr.portal.database.customization.icon.CustomIconRepository;
import com.getboostr.portal.database.gift.GiftEntity;
import com.getboostr.portal.database.gift.GiftRepository;
import com.getboostr.portal.database.gift.customization.GiftCustomizationDetailEntity;
import com.getboostr.portal.database.gift.customization.GiftCustomizationDetailRepository;
import com.getboostr.portal.database.gift.item.GiftItemDetailEntity;
import com.getboostr.portal.database.gift.item.GiftItemDetailRepository;
import com.getboostr.portal.database.gift.note.GiftNoteDetailEntity;
import com.getboostr.portal.database.gift.note.GiftNoteDetailRepository;
import com.getboostr.portal.database.gift.tracking.GiftTrackingRepository;
import com.getboostr.portal.database.note.NoteEntity;
import com.getboostr.portal.database.note.NoteRepository;
import com.getboostr.portal.rest.api.common.page.PageRequestValidationUtils;
import com.getboostr.portal.rest.api.gift.model.DraftGiftRequestModel;
import com.getboostr.portal.rest.api.gift.model.GiftResponseModel;
import com.getboostr.portal.rest.api.gift.model.MultiGiftModel;
import com.getboostr.portal.rest.api.gift.util.GiftAccessService;
import com.getboostr.portal.rest.api.gift.util.GiftResponseModelUtils;
import com.getboostr.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GiftService {
    private final GiftRepository giftRepository;
    private final GiftNoteDetailRepository noteDetailRepository;
    private final GiftItemDetailRepository itemDetailRepository;
    private final GiftCustomizationDetailRepository customizationDetailRepository;
    private final GiftTrackingRepository giftTrackingRepository;
    private final OrganizationAccountContactRepository contactRepository;
    private final NoteRepository noteRepository;
    private final CatalogueItemRepository catalogueItemRepository;
    private final CustomIconRepository customIconRepository;
    private final CustomBrandingTextRepository customBrandingTextRepository;
    private final GiftAccessService giftAccessService;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public GiftService(GiftRepository giftRepository, GiftNoteDetailRepository noteDetailRepository, GiftItemDetailRepository itemDetailRepository, GiftCustomizationDetailRepository customizationDetailRepository,
                       GiftTrackingRepository giftTrackingRepository, OrganizationAccountContactRepository contactRepository, NoteRepository noteRepository, CatalogueItemRepository catalogueItemRepository,
                       CustomIconRepository customIconRepository, CustomBrandingTextRepository customBrandingTextRepository,
                       GiftAccessService giftAccessService, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.giftRepository = giftRepository;
        this.noteDetailRepository = noteDetailRepository;
        this.itemDetailRepository = itemDetailRepository;
        this.customizationDetailRepository = customizationDetailRepository;
        this.giftTrackingRepository = giftTrackingRepository;
        this.contactRepository = contactRepository;
        this.noteRepository = noteRepository;
        this.catalogueItemRepository = catalogueItemRepository;
        this.customIconRepository = customIconRepository;
        this.customBrandingTextRepository = customBrandingTextRepository;
        this.giftAccessService = giftAccessService;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public MultiGiftModel getGifts(Integer pageOffset, Integer pageLimit) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);
        Page<GiftEntity> accessibleGifts = getAccessibleGifts(pageOffset, pageLimit);
        if (accessibleGifts.isEmpty()) {
            return MultiGiftModel.empty();
        }

        List<GiftResponseModel> responseModels = accessibleGifts
                .stream()
                .map(GiftResponseModelUtils::convertToResponseModel)
                .collect(Collectors.toList());
        return new MultiGiftModel(responseModels, accessibleGifts);
    }

    public GiftResponseModel getGift(UUID giftId) {
        GiftEntity foundGift = giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        giftAccessService.validateGiftAccess(foundGift, AccessOperation.READ);
        return GiftResponseModelUtils.convertToResponseModel(foundGift);
    }

    @Transactional
    public GiftResponseModel createDraftGift(DraftGiftRequestModel requestModel) {
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        validateRequestModel(loggedInUser, userMembership, requestModel);

        GiftEntity giftToSave = new GiftEntity(null, userMembership.getOrganizationAccountId(), loggedInUser.getUserId(), requestModel.getContactId());
        GiftEntity savedGift = giftRepository.save(giftToSave);

        saveDetails(savedGift, requestModel);
        return GiftResponseModelUtils.convertToResponseModel(savedGift);
    }

    @Transactional
    public void updateDraftGift(UUID giftId, DraftGiftRequestModel requestModel) {
        GiftEntity foundGift = giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (giftTrackingRepository.existsById(giftId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot edit a gift that has been sent");
        }
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        validateRequestModel(loggedInUser, userMembership, requestModel);

        foundGift.setContactId(requestModel.getContactId());
        saveDetails(foundGift, requestModel);
    }

    private void saveDetails(GiftEntity savedGift, DraftGiftRequestModel requestModel) {
        if (requestModel.getNoteId() != null) {
            GiftNoteDetailEntity noteDetailToSave = new GiftNoteDetailEntity(savedGift.getGiftId(), requestModel.getNoteId());
            noteDetailRepository.save(noteDetailToSave);
        }

        if (requestModel.getItemId() != null) {
            GiftItemDetailEntity itemDetailToSave = new GiftItemDetailEntity(savedGift.getGiftId(), requestModel.getItemId());
            itemDetailRepository.save(itemDetailToSave);
        }

        // TODO consider separate constraints for these
        if (requestModel.getCustomIconId() != null || requestModel.getCustomTextId() != null) {
            GiftCustomizationDetailEntity customizationDetailToSave = new GiftCustomizationDetailEntity(savedGift.getGiftId(), requestModel.getCustomIconId(), requestModel.getCustomTextId());
            customizationDetailRepository.save(customizationDetailToSave);
        }
    }

    private Page<GiftEntity> getAccessibleGifts(Integer pageOffset, Integer pageLimit) {
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return giftRepository.findAll(pageRequest);
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        if (membershipRetrievalService.isAuthenticateUserBasicOrPremiumMember()) {
            return giftRepository.findAllByRequestingUserId(loggedInUser.getUserId(), pageRequest);
        }

        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        return giftRepository.findAllByOrganizationAccountId(userMembership.getOrganizationAccountId(), pageRequest);
    }

    // TODO clean this method up after database is normalized
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
                if ((membershipRetrievalService.isAuthenticateUserBasicOrPremiumMember() && !customIcon.getUploaderId().equals(loggedInUser.getUserId()))
                        || !customIcon.getOrganizationAccountId().equals(userMembership.getOrganizationAccountId())) {
                    errors.add("The customIconId provided is not accessible by the requesting user");
                }
            } else {
                errors.add("The customIconId provided is invalid");
            }
        }

        if (requestModel.getCustomTextId() != null) {
            Optional<CustomBrandingTextEntity> optionalCustomBrandingText = customBrandingTextRepository.findById(requestModel.getCustomTextId());
            if (optionalCustomBrandingText.isPresent()) {
                CustomBrandingTextEntity customBrandingText = optionalCustomBrandingText.get();
                if ((membershipRetrievalService.isAuthenticateUserBasicOrPremiumMember() && !customBrandingText.getUploaderId().equals(loggedInUser.getUserId()))
                        || !customBrandingText.getOrganizationAccountId().equals(userMembership.getOrganizationAccountId())) {
                    errors.add("The customIconId provided is not accessible by the requesting user");
                }
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
                if ((membershipRetrievalService.isAuthenticateUserBasicOrPremiumMember() && !requestedNote.getUpdatedByUserId().equals(loggedInUser.getUserId()))
                        || !requestedNote.getOrganizationAccountId().equals(userMembership.getOrganizationAccountId())) {
                    errors.add("The noteId provided is not accessible by the requesting user");
                }
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

}
