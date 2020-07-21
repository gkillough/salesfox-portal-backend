package com.getboostr.portal.rest.customization.branding_text;

import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.rest.customization.branding_text.model.CustomBrandingTextRequestModel;
import com.getboostr.portal.rest.customization.branding_text.model.CustomBrandingTextResponseModel;
import com.getboostr.portal.rest.customization.branding_text.model.MultiCustomBrandingTextModel;
import com.getboostr.portal.database.customization.branding_text.CustomBrandingTextEntity;
import com.getboostr.portal.database.customization.branding_text.CustomBrandingTextOwnerEntity;
import com.getboostr.portal.database.customization.branding_text.CustomBrandingTextOwnerRepository;
import com.getboostr.portal.database.customization.branding_text.CustomBrandingTextRepository;
import com.getboostr.portal.rest.common.model.request.ActiveStatusPatchModel;
import com.getboostr.portal.rest.common.page.PageRequestValidationUtils;
import com.getboostr.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CustomBrandingTextService {
    public static final int CUSTOM_BRANDING_TEXT_CHAR_LIMIT = 50;

    private CustomBrandingTextRepository customBrandingTextRepository;
    private CustomBrandingTextOwnerRepository customBrandingTextOwnerRepository;
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public CustomBrandingTextService(CustomBrandingTextRepository customBrandingTextRepository, CustomBrandingTextOwnerRepository customBrandingTextOwnerRepository, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.customBrandingTextRepository = customBrandingTextRepository;
        this.customBrandingTextOwnerRepository = customBrandingTextOwnerRepository;
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
                .map(entity -> createResponseModel(entity, entity.getCustomBrandingTextOwnerEntity()))
                .collect(Collectors.toList());
        return new MultiCustomBrandingTextModel(responseModels, accessibleCustomBrandingTexts);
    }

    public CustomBrandingTextResponseModel getCustomBrandingText(UUID customBrandingTextId) {
        CustomBrandingTextEntity foundCustomBrandingText = customBrandingTextRepository.findById(customBrandingTextId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        CustomBrandingTextOwnerEntity nullableOwner = customBrandingTextOwnerRepository.findById(customBrandingTextId)
                .orElse(null);
        validateAccess(foundCustomBrandingText, nullableOwner);
        return createResponseModel(foundCustomBrandingText, nullableOwner);
    }

    @Transactional
    public CustomBrandingTextResponseModel createCustomBrandingText(CustomBrandingTextRequestModel requestModel) {
        validateRequestModel(requestModel);

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        CustomBrandingTextEntity entityToSave = new CustomBrandingTextEntity(null, requestModel.getCustomBrandingText(), userMembership.getOrganizationAccountId(), loggedInUser.getUserId(), true);
        CustomBrandingTextEntity savedEntity = customBrandingTextRepository.save(entityToSave);

        CustomBrandingTextOwnerEntity savedOwner = null;
        if (membershipRetrievalService.isAuthenticateUserBasicOrPremiumMember()) {
            CustomBrandingTextOwnerEntity ownerToSave = new CustomBrandingTextOwnerEntity(savedEntity.getCustomBrandingTextId(), loggedInUser.getUserId());
            savedOwner = customBrandingTextOwnerRepository.save(ownerToSave);
        }
        return createResponseModel(savedEntity, savedOwner);
    }

    @Transactional
    public void updateCustomBrandingText(UUID customBrandingTextId, CustomBrandingTextRequestModel requestModel) {
        CustomBrandingTextEntity foundCustomBrandingText = customBrandingTextRepository.findById(customBrandingTextId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        CustomBrandingTextOwnerEntity nullableOwner = customBrandingTextOwnerRepository.findById(customBrandingTextId)
                .orElse(null);
        validateAccess(foundCustomBrandingText, nullableOwner);
        validateRequestModel(requestModel);

        foundCustomBrandingText.setCustomBrandingText(requestModel.getCustomBrandingText());
        customBrandingTextRepository.save(foundCustomBrandingText);
    }

    @Transactional
    public void setActiveStatus(UUID customBrandingTextId, ActiveStatusPatchModel requestModel) {
        CustomBrandingTextEntity foundCustomBrandingText = customBrandingTextRepository.findById(customBrandingTextId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        CustomBrandingTextOwnerEntity nullableOwner = customBrandingTextOwnerRepository.findById(customBrandingTextId)
                .orElse(null);
        validateAccess(foundCustomBrandingText, nullableOwner);

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
        if (membershipRetrievalService.isAuthenticateUserBasicOrPremiumMember()) {
            return customBrandingTextRepository.findAllByOwningUserId(loggedInUser.getUserId(), pageRequest);
        }

        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        return customBrandingTextRepository.findAllByOrganizationAccountId(userMembership.getOrganizationAccountId(), pageRequest);
    }

    private void validateAccess(CustomBrandingTextEntity customBrandingTextEntity, @Nullable CustomBrandingTextOwnerEntity owner) {
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return;
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        if (membershipRetrievalService.isAuthenticateUserBasicOrPremiumMember()) {
            if (owner != null && loggedInUser.getUserId().equals(owner.getUserId())) {
                return;
            }
        } else {
            MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
            if (userMembership.getOrganizationAccountId().equals(customBrandingTextEntity.getOrganizationAccountId())) {
                return;
            }
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

    private CustomBrandingTextResponseModel createResponseModel(CustomBrandingTextEntity entity, @Nullable CustomBrandingTextOwnerEntity owner) {
        UUID ownerId = owner != null ? owner.getUserId() : null;
        return new CustomBrandingTextResponseModel(entity.getCustomBrandingTextId(), entity.getOrganizationAccountId(), ownerId, entity.getUploaderId(), entity.getCustomBrandingText(), entity.getIsActive());
    }

}
