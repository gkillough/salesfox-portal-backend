package ai.salesfox.portal.rest.api.gift;

import ai.salesfox.portal.common.enumeration.AccessOperation;
import ai.salesfox.portal.common.enumeration.GiftTrackingStatus;
import ai.salesfox.portal.common.time.PortalDateTimeUtils;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.catalogue.item.CatalogueItemRepository;
import ai.salesfox.portal.database.customization.branding_text.CustomBrandingTextEntity;
import ai.salesfox.portal.database.customization.branding_text.CustomBrandingTextRepository;
import ai.salesfox.portal.database.customization.branding_text.restriction.CustomBrandingTextOrgAccountRestrictionEntity;
import ai.salesfox.portal.database.customization.icon.CustomIconEntity;
import ai.salesfox.portal.database.customization.icon.CustomIconRepository;
import ai.salesfox.portal.database.customization.icon.restriction.CustomIconOrganizationAccountRestrictionEntity;
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
import ai.salesfox.portal.database.gift.restriction.GiftOrgAccountRestrictionEntity;
import ai.salesfox.portal.database.gift.restriction.GiftOrgAccountRestrictionRepository;
import ai.salesfox.portal.database.gift.restriction.GiftUserRestrictionEntity;
import ai.salesfox.portal.database.gift.restriction.GiftUserRestrictionRepository;
import ai.salesfox.portal.database.gift.tracking.GiftTrackingEntity;
import ai.salesfox.portal.database.gift.tracking.GiftTrackingRepository;
import ai.salesfox.portal.database.note.NoteEntity;
import ai.salesfox.portal.database.note.NoteRepository;
import ai.salesfox.portal.database.note.restriction.NoteOrganizationAccountRestrictionEntity;
import ai.salesfox.portal.rest.api.common.page.PageRequestValidationUtils;
import ai.salesfox.portal.rest.api.gift.model.DraftGiftRequestModel;
import ai.salesfox.portal.rest.api.gift.model.GiftResponseModel;
import ai.salesfox.portal.rest.api.gift.model.MultiGiftModel;
import ai.salesfox.portal.rest.api.gift.util.GiftAccessService;
import ai.salesfox.portal.rest.api.gift.util.GiftResponseModelUtils;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
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
    private final GiftNoteDetailRepository noteDetailRepository;
    private final GiftItemDetailRepository itemDetailRepository;
    private final GiftCustomIconDetailRepository customIconDetailRepository;
    private final GiftCustomTextDetailRepository giftCustomTextDetailRepository;
    private final GiftTrackingRepository giftTrackingRepository;
    private final GiftOrgAccountRestrictionRepository giftOrgAcctRestrictionRepository;
    private final GiftUserRestrictionRepository giftUserRestrictionRepository;
    private final NoteRepository noteRepository;
    private final CatalogueItemRepository catalogueItemRepository;
    private final CustomIconRepository customIconRepository;
    private final CustomBrandingTextRepository customBrandingTextRepository;
    private final GiftAccessService giftAccessService;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public GiftService(
            GiftRepository giftRepository,
            GiftNoteDetailRepository noteDetailRepository,
            GiftItemDetailRepository itemDetailRepository,
            GiftCustomIconDetailRepository customIconDetailRepository,
            GiftCustomTextDetailRepository giftCustomTextDetailRepository,
            GiftTrackingRepository giftTrackingRepository,
            GiftOrgAccountRestrictionRepository giftOrgAcctRestrictionRepository,
            GiftUserRestrictionRepository giftUserRestrictionRepository,
            NoteRepository noteRepository,
            CatalogueItemRepository catalogueItemRepository,
            CustomIconRepository customIconRepository,
            CustomBrandingTextRepository customBrandingTextRepository,
            GiftAccessService giftAccessService,
            HttpSafeUserMembershipRetrievalService membershipRetrievalService
    ) {
        this.giftRepository = giftRepository;
        this.noteDetailRepository = noteDetailRepository;
        this.itemDetailRepository = itemDetailRepository;
        this.customIconDetailRepository = customIconDetailRepository;
        this.giftCustomTextDetailRepository = giftCustomTextDetailRepository;
        this.giftTrackingRepository = giftTrackingRepository;
        this.giftOrgAcctRestrictionRepository = giftOrgAcctRestrictionRepository;
        this.giftUserRestrictionRepository = giftUserRestrictionRepository;
        this.noteRepository = noteRepository;
        this.catalogueItemRepository = catalogueItemRepository;
        this.customIconRepository = customIconRepository;
        this.customBrandingTextRepository = customBrandingTextRepository;
        this.giftAccessService = giftAccessService;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public MultiGiftModel getGifts(Integer pageOffset, Integer pageLimit, List<String> giftStatuses) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);
        validateGiftStatusesFilter(giftStatuses);

        Page<GiftEntity> accessibleGifts = getAccessibleGifts(pageOffset, pageLimit, giftStatuses);
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
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        giftAccessService.validateGiftAccess(foundGift, loggedInUser, AccessOperation.READ);

        return GiftResponseModelUtils.convertToResponseModel(foundGift);
    }

    @Transactional
    public GiftResponseModel createDraftGift(DraftGiftRequestModel requestModel) {
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        validateRequestModel(loggedInUser, userMembership, requestModel);

        GiftEntity giftToSave = new GiftEntity(null, loggedInUser.getUserId());
        GiftEntity savedGift = giftRepository.save(giftToSave);
        saveDetails(savedGift, requestModel);

        OffsetDateTime dateCreated = PortalDateTimeUtils.getCurrentDateTime();
        GiftTrackingEntity giftTrackingToSave = new GiftTrackingEntity(savedGift.getGiftId(), GiftTrackingStatus.DRAFT.name(), loggedInUser.getUserId(), dateCreated, dateCreated);
        GiftTrackingEntity savedGiftTracking = giftTrackingRepository.save(giftTrackingToSave);
        savedGift.setGiftTrackingEntity(savedGiftTracking);

        // FIXME determine if gifts should be restricted to a specific user
        boolean restrictToUser = false;
        if (restrictToUser) {
            GiftUserRestrictionEntity userRestrictionToSave = new GiftUserRestrictionEntity(savedGift.getGiftId(), loggedInUser.getUserId());
            GiftUserRestrictionEntity savedUserRestriction = giftUserRestrictionRepository.save(userRestrictionToSave);
            savedGift.setGiftUserRestrictionEntity(savedUserRestriction);
        } else {
            GiftOrgAccountRestrictionEntity orgAcctRestrictionToSave = new GiftOrgAccountRestrictionEntity(savedGift.getGiftId(), userMembership.getOrganizationAccountId());
            GiftOrgAccountRestrictionEntity savedOrgAcctRestriction = giftOrgAcctRestrictionRepository.save(orgAcctRestrictionToSave);
            savedGift.setGiftOrgAccountRestrictionEntity(savedOrgAcctRestriction);
        }

        savedGift.setRequestingUserEntity(loggedInUser);
        return GiftResponseModelUtils.convertToResponseModel(savedGift);
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

    private Page<GiftEntity> getAccessibleGifts(Integer pageOffset, Integer pageLimit, List<String> giftStatuses) {
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return giftRepository.findAll(pageRequest);
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        return giftRepository.findAccessibleGiftsByStatuses(userMembership.getOrganizationAccountId(), loggedInUser.getUserId(), giftStatuses, pageRequest);
    }

    private void validateGiftStatusesFilter(Collection<String> giftStatuses) {
        if (null == giftStatuses) {
            return;
        }

        List<String> invalidStatuses = new ArrayList<>(giftStatuses.size());
        for (String giftStatus : giftStatuses) {
            if (StringUtils.isBlank(giftStatus) || !EnumUtils.isValidEnum(GiftTrackingStatus.class, giftStatus)) {
                invalidStatuses.add(giftStatus);
            }
        }

        if (!invalidStatuses.isEmpty()) {
            String invalidStatusesString = String.join(", ", invalidStatuses);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Invalid status(es): [%s]. Valid statuses: [%s]", invalidStatusesString, Arrays.toString(GiftTrackingStatus.values())));
        }
    }

    // TODO clean this method up after a common restriction interface is implemented
    private void validateRequestModel(UserEntity loggedInUser, MembershipEntity userMembership, DraftGiftRequestModel requestModel) {
        List<String> errors = new ArrayList<>();

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
                        userMembership,
                        customIcon.getCustomIconOrganizationAccountRestrictionEntity(),
                        CustomIconOrganizationAccountRestrictionEntity::getOrganizationAccountId
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
                        userMembership,
                        customBrandingText.getCustomBrandingTextOrgAccountRestrictionEntity(),
                        CustomBrandingTextOrgAccountRestrictionEntity::getOrgAccountId
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
                        userMembership,
                        requestedNote.getNoteOrganizationAccountRestrictionEntity(),
                        NoteOrganizationAccountRestrictionEntity::getOrganizationAccountId
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

    private <O> void validateRestrictedEntity(String idName, List<String> errors, MembershipEntity userMembership, O orgAcctRestriction, Function<O, UUID> orgAcctIdExtractor) {
        if (orgAcctRestriction != null && !orgAcctIdExtractor.apply(orgAcctRestriction).equals(userMembership.getOrganizationAccountId())) {
            errors.add(String.format("The %s provided is not accessible from this organization account", idName));
        }
    }

}
