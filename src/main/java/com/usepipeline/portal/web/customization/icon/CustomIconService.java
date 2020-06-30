package com.usepipeline.portal.web.customization.icon;

import com.usepipeline.portal.database.account.entity.MembershipEntity;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.customization.icon.CustomIconEntity;
import com.usepipeline.portal.database.customization.icon.CustomIconOwnerEntity;
import com.usepipeline.portal.database.customization.icon.CustomIconOwnerRepository;
import com.usepipeline.portal.database.customization.icon.CustomIconRepository;
import com.usepipeline.portal.web.common.model.request.ActiveStatusPatchModel;
import com.usepipeline.portal.web.common.page.PageRequestValidationUtils;
import com.usepipeline.portal.web.customization.icon.model.CustomIconRequestModel;
import com.usepipeline.portal.web.customization.icon.model.CustomIconResponseModel;
import com.usepipeline.portal.web.customization.icon.model.MultiCustomIconResponseModel;
import com.usepipeline.portal.web.util.HttpSafeUserMembershipRetrievalService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CustomIconService {
    private CustomIconRepository customIconRepository;
    private CustomIconOwnerRepository customIconOwnerRepository;
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private CustomIconAccessService customIconAccessService;

    @Autowired
    public CustomIconService(CustomIconRepository customIconRepository, CustomIconOwnerRepository customIconOwnerRepository, HttpSafeUserMembershipRetrievalService membershipRetrievalService, CustomIconAccessService customIconAccessService) {
        this.customIconRepository = customIconRepository;
        this.customIconOwnerRepository = customIconOwnerRepository;
        this.membershipRetrievalService = membershipRetrievalService;
        this.customIconAccessService = customIconAccessService;
    }

    public MultiCustomIconResponseModel getCustomIcons(Integer pageOffset, Integer pageLimit) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);

        Page<CustomIconEntity> accessibleCustomIcons = getAccessibleCustomIcons(pageOffset, pageLimit);
        if (accessibleCustomIcons.isEmpty()) {
            return MultiCustomIconResponseModel.empty();
        }

        List<CustomIconResponseModel> responseModels = accessibleCustomIcons
                .stream()
                .map(entity -> convertToResponseModel(entity, entity.getCustomIconOwnerEntity()))
                .collect(Collectors.toList());
        return new MultiCustomIconResponseModel(responseModels, accessibleCustomIcons);
    }

    public CustomIconResponseModel getCustomIcon(UUID customIconId) {
        CustomIconEntity foundCustomIcon = customIconRepository.findById(customIconId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        CustomIconOwnerEntity nullableOwner = customIconOwnerRepository.findById(customIconId)
                .orElse(null);
        customIconAccessService.validateImageAccess(foundCustomIcon, nullableOwner);
        return convertToResponseModel(foundCustomIcon, nullableOwner);
    }

    @Transactional
    public CustomIconResponseModel createCustomIcon(CustomIconRequestModel requestModel) {
        validateRequestModel(requestModel);
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);

        CustomIconEntity customIconToSave = new CustomIconEntity(null, requestModel.getLabel(), userMembership.getOrganizationAccountId(), loggedInUser.getUserId(), true);
        CustomIconEntity savedCustomIcon = customIconRepository.save(customIconToSave);

        CustomIconOwnerEntity nullableOwner = null;
        if (membershipRetrievalService.isAuthenticateUserBasicOrPremiumMember()) {
            CustomIconOwnerEntity customIconOwnerToSave = new CustomIconOwnerEntity(savedCustomIcon.getCustomIconId(), loggedInUser.getUserId());
            nullableOwner = customIconOwnerRepository.save(customIconOwnerToSave);
        }

        return convertToResponseModel(savedCustomIcon, nullableOwner);
    }

    @Transactional
    public void updateCustomIcon(UUID customIconId, CustomIconRequestModel requestModel) {
        CustomIconEntity foundCustomIcon = customIconRepository.findById(customIconId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        CustomIconOwnerEntity nullableOwner = customIconOwnerRepository.findById(customIconId)
                .orElse(null);
        customIconAccessService.validateImageAccess(foundCustomIcon, nullableOwner);

        validateRequestModel(requestModel);
        foundCustomIcon.setLabel(requestModel.getLabel());
        customIconRepository.save(foundCustomIcon);
    }

    @Transactional
    public void setActiveStatus(UUID customIconId, ActiveStatusPatchModel requestModel) {
        CustomIconEntity foundCustomIcon = customIconRepository.findById(customIconId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        CustomIconOwnerEntity nullableOwner = customIconOwnerRepository.findById(customIconId)
                .orElse(null);
        customIconAccessService.validateImageAccess(foundCustomIcon, nullableOwner);

        if (requestModel.getActiveStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The request field 'activeStatus' cannot be null");
        }

        foundCustomIcon.setIsActive(requestModel.getActiveStatus());
        customIconRepository.save(foundCustomIcon);
    }

    private Page<CustomIconEntity> getAccessibleCustomIcons(Integer pageOffset, Integer pageLimit) {
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        if (membershipRetrievalService.isAuthenticatedUserPipelineAdmin()) {
            return customIconRepository.findAll(pageRequest);
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        if (membershipRetrievalService.isAuthenticateUserBasicOrPremiumMember()) {
            return customIconRepository.findAllByOwningUserId(loggedInUser.getUserId(), pageRequest);
        }

        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        return customIconRepository.findAllByOrganizationAccountId(userMembership.getOrganizationAccountId(), pageRequest);
    }

    private void validateRequestModel(CustomIconRequestModel requestModel) {
        if (StringUtils.isBlank(requestModel.getLabel())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The request field 'label' cannot be blank");
        }
    }

    private CustomIconResponseModel convertToResponseModel(CustomIconEntity entity, CustomIconOwnerEntity nullableOwner) {
        UUID ownerId = nullableOwner != null ? nullableOwner.getUserId() : null;
        return new CustomIconResponseModel(entity.getCustomIconId(), entity.getLabel(), entity.getOrganizationAccountId(), ownerId, entity.getUploaderId(), entity.getIsActive());
    }

}
