package com.getboostr.portal.rest.api.customization.icon;

import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.customization.icon.CustomIconEntity;
import com.getboostr.portal.database.customization.icon.CustomIconRepository;
import com.getboostr.portal.database.customization.icon.restriction.CustomIconOrganizationAccountRepository;
import com.getboostr.portal.database.customization.icon.restriction.CustomIconOrganizationAccountRestrictionEntity;
import com.getboostr.portal.database.customization.icon.restriction.CustomIconUserRestrictionEntity;
import com.getboostr.portal.database.customization.icon.restriction.CustomIconUserRestrictionRepository;
import com.getboostr.portal.rest.api.common.model.request.ActiveStatusPatchModel;
import com.getboostr.portal.rest.api.common.model.request.RestrictionModel;
import com.getboostr.portal.rest.api.common.page.PageRequestValidationUtils;
import com.getboostr.portal.rest.api.customization.icon.model.CustomIconRequestModel;
import com.getboostr.portal.rest.api.customization.icon.model.CustomIconResponseModel;
import com.getboostr.portal.rest.api.customization.icon.model.MultiCustomIconResponseModel;
import com.getboostr.portal.rest.api.user.common.model.ViewUserModel;
import com.getboostr.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.apache.commons.lang3.StringUtils;
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
public class CustomIconService {
    private final CustomIconRepository customIconRepository;
    private final CustomIconOrganizationAccountRepository customIconOrgAcctRepository;
    private final CustomIconUserRestrictionRepository customIconUserRestrictionRepository;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private final CustomIconAccessService customIconAccessService;

    @Autowired
    public CustomIconService(CustomIconRepository customIconRepository, CustomIconOrganizationAccountRepository customIconOrgAcctRepository, CustomIconUserRestrictionRepository customIconUserRestrictionRepository,
                             HttpSafeUserMembershipRetrievalService membershipRetrievalService, CustomIconAccessService customIconAccessService) {
        this.customIconRepository = customIconRepository;
        this.customIconOrgAcctRepository = customIconOrgAcctRepository;
        this.customIconUserRestrictionRepository = customIconUserRestrictionRepository;
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
                .map(this::convertToResponseModel)
                .collect(Collectors.toList());
        return new MultiCustomIconResponseModel(responseModels, accessibleCustomIcons);
    }

    public CustomIconResponseModel getCustomIcon(UUID customIconId) {
        CustomIconEntity foundCustomIcon = customIconRepository.findById(customIconId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        customIconAccessService.validateImageAccess(foundCustomIcon);
        return convertToResponseModel(foundCustomIcon);
    }

    @Transactional
    public CustomIconResponseModel createCustomIcon(CustomIconRequestModel requestModel) {
        validateRequestModel(requestModel);
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();

        CustomIconEntity customIconToSave = new CustomIconEntity(null, requestModel.getLabel(), loggedInUser.getUserId(), true);
        CustomIconEntity savedCustomIcon = customIconRepository.save(customIconToSave);

        if (membershipRetrievalService.isAuthenticateUserBasicOrPremiumMember()) {
            CustomIconUserRestrictionEntity userRestrictionToSave = new CustomIconUserRestrictionEntity(savedCustomIcon.getCustomIconId(), loggedInUser.getUserId());
            CustomIconUserRestrictionEntity savedUserRestriction = customIconUserRestrictionRepository.save(userRestrictionToSave);
            savedCustomIcon.setCustomIconUserRestrictionEntity(savedUserRestriction);
        } else {
            MembershipEntity userMembership = loggedInUser.getMembershipEntity();
            CustomIconOrganizationAccountRestrictionEntity orgAcctRestrictionToSave = new CustomIconOrganizationAccountRestrictionEntity(savedCustomIcon.getCustomIconId(), userMembership.getOrganizationAccountId());
            CustomIconOrganizationAccountRestrictionEntity savedOrgAcctRestriction = customIconOrgAcctRepository.save(orgAcctRestrictionToSave);
            savedCustomIcon.setCustomIconOrganizationAccountRestrictionEntity(savedOrgAcctRestriction);
        }

        savedCustomIcon.setUploaderEntity(loggedInUser);
        return convertToResponseModel(savedCustomIcon);
    }

    @Transactional
    public void updateCustomIcon(UUID customIconId, CustomIconRequestModel requestModel) {
        CustomIconEntity foundCustomIcon = customIconRepository.findById(customIconId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        customIconAccessService.validateImageAccess(foundCustomIcon);
        validateRequestModel(requestModel);

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        foundCustomIcon.setLabel(requestModel.getLabel());
        foundCustomIcon.setUploaderId(loggedInUser.getUserId());
        customIconRepository.save(foundCustomIcon);
    }

    @Transactional
    public void setActiveStatus(UUID customIconId, ActiveStatusPatchModel requestModel) {
        CustomIconEntity foundCustomIcon = customIconRepository.findById(customIconId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        customIconAccessService.validateImageAccess(foundCustomIcon);

        if (requestModel.getActiveStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The request field 'activeStatus' cannot be null");
        }

        foundCustomIcon.setIsActive(requestModel.getActiveStatus());
        customIconRepository.save(foundCustomIcon);
    }

    private Page<CustomIconEntity> getAccessibleCustomIcons(Integer pageOffset, Integer pageLimit) {
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return customIconRepository.findAll(pageRequest);
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        return customIconRepository.findAccessibleCustomIcons(userMembership.getOrganizationAccountId(), loggedInUser.getUserId(), pageRequest);
    }

    private void validateRequestModel(CustomIconRequestModel requestModel) {
        if (StringUtils.isBlank(requestModel.getLabel())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The request field 'label' cannot be blank");
        }
    }

    private CustomIconResponseModel convertToResponseModel(CustomIconEntity entity) {
        ViewUserModel uploaderModel = Optional.ofNullable(entity.getUploaderEntity())
                .map(ViewUserModel::fromEntity)
                .orElse(null);

        RestrictionModel restrictionModel = new RestrictionModel();
        Optional.ofNullable(entity.getCustomIconOrganizationAccountRestrictionEntity())
                .map(CustomIconOrganizationAccountRestrictionEntity::getOrganizationAccountId)
                .ifPresent(restrictionModel::setOrganizationAccountId);
        Optional.ofNullable(entity.getCustomIconUserRestrictionEntity())
                .map(CustomIconUserRestrictionEntity::getUserId)
                .ifPresent(restrictionModel::setUserId);
        return new CustomIconResponseModel(entity.getCustomIconId(), entity.getLabel(), uploaderModel, entity.getIsActive(), restrictionModel);
    }

}
