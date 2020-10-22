package ai.salesfox.portal.rest.api.customization.branding_text;

import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.customization.branding_text.CustomBrandingTextEntity;
import ai.salesfox.portal.database.customization.branding_text.CustomBrandingTextRepository;
import ai.salesfox.portal.database.customization.branding_text.restriction.CustomBrandingTextOrgAccountRestrictionEntity;
import ai.salesfox.portal.database.customization.branding_text.restriction.CustomBrandingTextOrgAccountRestrictionRepository;
import ai.salesfox.portal.database.gift.customization.GiftCustomTextDetailEntity;
import ai.salesfox.portal.database.gift.customization.GiftCustomTextDetailRepository;
import ai.salesfox.portal.database.gift.tracking.GiftTrackingEntity;
import ai.salesfox.portal.database.gift.tracking.GiftTrackingRepository;
import ai.salesfox.portal.rest.api.common.model.request.ActiveStatusPatchModel;
import ai.salesfox.portal.rest.api.common.model.request.RestrictionModel;
import ai.salesfox.portal.rest.api.common.page.PageRequestValidationUtils;
import ai.salesfox.portal.rest.api.customization.branding_text.model.CustomBrandingTextRequestModel;
import ai.salesfox.portal.rest.api.customization.branding_text.model.CustomBrandingTextResponseModel;
import ai.salesfox.portal.rest.api.customization.branding_text.model.MultiCustomBrandingTextModel;
import ai.salesfox.portal.rest.api.user.common.model.UserSummaryModel;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CustomBrandingTextService {
    public static final int CUSTOM_BRANDING_TEXT_CHAR_LIMIT = 50;

    private final CustomBrandingTextRepository customBrandingTextRepository;
    private final CustomBrandingTextOrgAccountRestrictionRepository customBrandingTextOrgAcctRestrictionRepository;
    private final GiftTrackingRepository giftTrackingRepository;
    private final GiftCustomTextDetailRepository giftCustomTextDetailRepository;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public CustomBrandingTextService(CustomBrandingTextRepository customBrandingTextRepository, CustomBrandingTextOrgAccountRestrictionRepository customBrandingTextOrgAcctRestrictionRepository,
                                     GiftTrackingRepository giftTrackingRepository, GiftCustomTextDetailRepository giftCustomTextDetailRepository, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.customBrandingTextRepository = customBrandingTextRepository;
        this.customBrandingTextOrgAcctRestrictionRepository = customBrandingTextOrgAcctRestrictionRepository;
        this.giftTrackingRepository = giftTrackingRepository;
        this.giftCustomTextDetailRepository = giftCustomTextDetailRepository;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public MultiCustomBrandingTextModel getCustomBrandingTexts(Integer pageOffset, Integer pageLimit) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);

        Page<CustomBrandingTextEntity> accessibleCustomBrandingTexts = getAccessibleCustomBrandingTexts(pageOffset, pageLimit);
        if (accessibleCustomBrandingTexts.isEmpty()) {
            return MultiCustomBrandingTextModel.empty();
        }

        List<CustomBrandingTextResponseModel> responseModels = accessibleCustomBrandingTexts
                .stream()
                .map(this::createResponseModel)
                .collect(Collectors.toList());
        return new MultiCustomBrandingTextModel(responseModels, accessibleCustomBrandingTexts);
    }

    public CustomBrandingTextResponseModel getCustomBrandingText(UUID customBrandingTextId) {
        CustomBrandingTextEntity foundCustomBrandingText = customBrandingTextRepository.findById(customBrandingTextId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        validateAccess(foundCustomBrandingText);
        return createResponseModel(foundCustomBrandingText);
    }

    @Transactional
    public CustomBrandingTextResponseModel createCustomBrandingText(CustomBrandingTextRequestModel requestModel) {
        validateRequestModel(requestModel);

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        CustomBrandingTextEntity entityToSave = new CustomBrandingTextEntity(null, requestModel.getCustomBrandingText(), loggedInUser.getUserId(), true);
        CustomBrandingTextEntity savedEntity = customBrandingTextRepository.save(entityToSave);

        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        CustomBrandingTextOrgAccountRestrictionEntity orgAcctRestrictionToSave = new CustomBrandingTextOrgAccountRestrictionEntity(savedEntity.getCustomBrandingTextId(), userMembership.getOrganizationAccountId());
        CustomBrandingTextOrgAccountRestrictionEntity savedOrgAcctRestriction = customBrandingTextOrgAcctRestrictionRepository.save(orgAcctRestrictionToSave);
        savedEntity.setCustomBrandingTextOrgAccountRestrictionEntity(savedOrgAcctRestriction);

        savedEntity.setUploaderEntity(loggedInUser);
        return createResponseModel(savedEntity);
    }

    @Transactional
    public void updateCustomBrandingText(UUID customBrandingTextId, CustomBrandingTextRequestModel requestModel) {
        CustomBrandingTextEntity foundCustomBrandingText = customBrandingTextRepository.findById(customBrandingTextId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        validateAccess(foundCustomBrandingText);
        validateCustomBrandingTextGiftStatus(customBrandingTextId);
        validateRequestModel(requestModel);

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        foundCustomBrandingText.setCustomBrandingText(requestModel.getCustomBrandingText());
        foundCustomBrandingText.setUploaderId(loggedInUser.getUserId());
        customBrandingTextRepository.save(foundCustomBrandingText);
    }

    @Transactional
    public void setActiveStatus(UUID customBrandingTextId, ActiveStatusPatchModel requestModel) {
        CustomBrandingTextEntity foundCustomBrandingText = customBrandingTextRepository.findById(customBrandingTextId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        validateAccess(foundCustomBrandingText);

        if (requestModel.getActiveStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The request field 'activeStatus' cannot be null");
        }

        foundCustomBrandingText.setIsActive(requestModel.getActiveStatus());
        customBrandingTextRepository.save(foundCustomBrandingText);
    }

    private Page<CustomBrandingTextEntity> getAccessibleCustomBrandingTexts(Integer pageOffset, Integer pageLimit) {
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return customBrandingTextRepository.findAll(pageRequest);
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        return customBrandingTextRepository.findAccessibleCustomBrandingTexts(userMembership.getOrganizationAccountId(), pageRequest);
    }

    private void validateAccess(CustomBrandingTextEntity customBrandingTextEntity) {
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return;
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();

        CustomBrandingTextOrgAccountRestrictionEntity orgAcctRestriction = customBrandingTextEntity.getCustomBrandingTextOrgAccountRestrictionEntity();
        if (null != orgAcctRestriction && orgAcctRestriction.getOrgAccountId().equals(userMembership.getOrganizationAccountId())) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    private void validateRequestModel(CustomBrandingTextRequestModel requestModel) {
        if (requestModel.getCustomBrandingText() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The request field 'customBrandingText' cannot be null");
        }

        if (requestModel.getCustomBrandingText().length() > CUSTOM_BRANDING_TEXT_CHAR_LIMIT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The custom branding text cannot exceed %d characters", CUSTOM_BRANDING_TEXT_CHAR_LIMIT));
        }
    }

    private void validateCustomBrandingTextGiftStatus(UUID customTextId) {
        List<UUID> customTextGiftIds = giftCustomTextDetailRepository.findByCustomTextId(customTextId)
                .stream()
                .map(GiftCustomTextDetailEntity::getGiftId)
                .collect(Collectors.toList());
        if (!customTextGiftIds.isEmpty()) {
            boolean hasCustomTextBeenSubmitted = giftTrackingRepository.findAllById(customTextGiftIds)
                    .stream()
                    .anyMatch(GiftTrackingEntity::isSubmitted);
            if (hasCustomTextBeenSubmitted) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update a custom branding text that has been submitted with a gift");
            }
        }
    }

    private CustomBrandingTextResponseModel createResponseModel(CustomBrandingTextEntity entity) {
        UserSummaryModel uploaderModel = Optional.ofNullable(entity.getUploaderEntity())
                .map(UserSummaryModel::fromEntity)
                .orElse(null);

        RestrictionModel restrictionModel = new RestrictionModel();
        Optional.ofNullable(entity.getCustomBrandingTextOrgAccountRestrictionEntity())
                .map(CustomBrandingTextOrgAccountRestrictionEntity::getOrgAccountId)
                .ifPresent(restrictionModel::setOrganizationAccountId);
        return new CustomBrandingTextResponseModel(entity.getCustomBrandingTextId(), entity.getCustomBrandingText(), uploaderModel, entity.getIsActive(), restrictionModel);
    }

}
