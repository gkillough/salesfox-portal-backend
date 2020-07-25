package com.getboostr.portal.rest.api.catalogue;

import com.getboostr.portal.common.service.catalogue.CatalogueItemAccessUtils;
import com.getboostr.portal.common.service.icon.LocalIconManager;
import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.account.repository.UserRepository;
import com.getboostr.portal.database.catalogue.icon.CatalogueItemIconEntity;
import com.getboostr.portal.database.catalogue.icon.CatalogueItemIconRepository;
import com.getboostr.portal.database.catalogue.item.CatalogueItemEntity;
import com.getboostr.portal.database.catalogue.item.CatalogueItemRepository;
import com.getboostr.portal.database.catalogue.restriction.CatalogueItemOrganizationAccountRestrictionEntity;
import com.getboostr.portal.database.catalogue.restriction.CatalogueItemOrganizationAccountRestrictionRepository;
import com.getboostr.portal.database.catalogue.restriction.CatalogueItemUserRestrictionEntity;
import com.getboostr.portal.database.catalogue.restriction.CatalogueItemUserRestrictionRepository;
import com.getboostr.portal.database.organization.account.OrganizationAccountRepository;
import com.getboostr.portal.rest.api.catalogue.model.CatalogueItemRequestModel;
import com.getboostr.portal.rest.api.catalogue.model.CatalogueItemResponseModel;
import com.getboostr.portal.rest.api.catalogue.model.MultiCatalogueItemModel;
import com.getboostr.portal.rest.api.common.model.request.ActiveStatusPatchModel;
import com.getboostr.portal.rest.api.common.model.request.RestrictionModel;
import com.getboostr.portal.rest.api.common.page.PageRequestValidationUtils;
import com.getboostr.portal.rest.api.image.HttpSafeImageUtility;
import com.getboostr.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Component
public class CatalogueService {
    private final CatalogueItemRepository catalogueItemRepository;
    private final CatalogueItemOrganizationAccountRestrictionRepository catItemOrgAcctRestrictionRepository;
    private final CatalogueItemUserRestrictionRepository catItemUserRestrictionRepository;
    private final CatalogueItemIconRepository catalogueItemIconRepository;
    private final OrganizationAccountRepository organizationAccountRepository;
    private final UserRepository userRepository;
    private final HttpSafeImageUtility imageUtility;
    private final LocalIconManager localIconManager;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public CatalogueService(CatalogueItemRepository catalogueItemRepository, CatalogueItemOrganizationAccountRestrictionRepository catItemOrgAcctRestrictionRepository,
                            CatalogueItemUserRestrictionRepository catItemUserRestrictionRepository, CatalogueItemIconRepository catalogueItemIconRepository,
                            OrganizationAccountRepository organizationAccountRepository, UserRepository userRepository,
                            HttpSafeImageUtility imageUtility, LocalIconManager localIconManager, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.catalogueItemRepository = catalogueItemRepository;
        this.catItemOrgAcctRestrictionRepository = catItemOrgAcctRestrictionRepository;
        this.catItemUserRestrictionRepository = catItemUserRestrictionRepository;
        this.catalogueItemIconRepository = catalogueItemIconRepository;
        this.organizationAccountRepository = organizationAccountRepository;
        this.userRepository = userRepository;
        this.imageUtility = imageUtility;
        this.localIconManager = localIconManager;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public MultiCatalogueItemModel getItems(Integer pageOffset, Integer pageLimit) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);

        Page<CatalogueItemEntity> accessibleItems = getAccessibleItems(pageOffset, pageLimit);
        if (accessibleItems.isEmpty()) {
            return MultiCatalogueItemModel.empty();
        }

        List<CatalogueItemResponseModel> catalogueItems = new ArrayList<>(accessibleItems.getNumberOfElements());
        for (CatalogueItemEntity item : accessibleItems) {
            CatalogueItemResponseModel responseItem = convertToResponseModel(item);
            catalogueItems.add(responseItem);
        }
        return new MultiCatalogueItemModel(catalogueItems, accessibleItems);
    }

    public CatalogueItemResponseModel getItem(UUID itemId) {
        CatalogueItemEntity foundItem = catalogueItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        validateItemAccess(foundItem);
        return convertToResponseModel(foundItem);
    }

    @Transactional
    public CatalogueItemResponseModel addItem(CatalogueItemRequestModel itemRequestModel) {
        validateRequestModel(itemRequestModel);

        RestrictionModel restrictionRequestModel = itemRequestModel.getRestriction();
        boolean isRestricted = restrictionRequestModel != null;
        CatalogueItemEntity newItem = new CatalogueItemEntity(null, itemRequestModel.getName(), itemRequestModel.getPrice(), itemRequestModel.getQuantity(), null, true);

        CatalogueItemEntity savedItem = catalogueItemRepository.save(newItem);

        if (isRestricted) {
            if (null != restrictionRequestModel.getOrganizationAccountId()) {
                CatalogueItemOrganizationAccountRestrictionEntity orgAcctRestriction = new CatalogueItemOrganizationAccountRestrictionEntity(savedItem.getItemId(), restrictionRequestModel.getOrganizationAccountId());
                catItemOrgAcctRestrictionRepository.save(orgAcctRestriction);
            }

            if (null != restrictionRequestModel.getUserId()) {
                CatalogueItemUserRestrictionEntity userRestriction = new CatalogueItemUserRestrictionEntity(savedItem.getItemId(), restrictionRequestModel.getUserId());
                catItemUserRestrictionRepository.save(userRestriction);
            }
        }
        return convertToResponseModel(savedItem);
    }

    @Transactional
    public void setItemIcon(UUID itemId, MultipartFile iconFile) {
        CatalogueItemEntity exitingItem = catalogueItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        File savedIcon = imageUtility.saveImage(iconFile);
        String savedIconName = FilenameUtils.getName(savedIcon.getName());
        CatalogueItemIconEntity iconEntityToSave = new CatalogueItemIconEntity(null, savedIconName);
        CatalogueItemIconEntity savedIconEntity = catalogueItemIconRepository.save(iconEntityToSave);

        exitingItem.setIconId(savedIconEntity.getIconId());
        catalogueItemRepository.save(exitingItem);
    }

    @Transactional
    public void updateItem(UUID itemId, CatalogueItemRequestModel itemRequestModel) {
        CatalogueItemEntity existingItem = catalogueItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        validateRequestModel(itemRequestModel);

        RestrictionModel restrictionRequestModel = itemRequestModel.getRestriction();
        boolean isRestricted = restrictionRequestModel != null;

        existingItem.setName(itemRequestModel.getName());
        existingItem.setPrice(itemRequestModel.getPrice());
        existingItem.setQuantity(itemRequestModel.getQuantity());
        CatalogueItemEntity savedItem = catalogueItemRepository.save(existingItem);

        if (isRestricted) {
            CatalogueItemOrganizationAccountRestrictionEntity orgAcctRestriction = savedItem.getCatalogueItemOrganizationAccountRestrictionEntity();
            if (null != restrictionRequestModel.getOrganizationAccountId()) {
                CatalogueItemOrganizationAccountRestrictionEntity orgAcctRestrictionToSave = Optional.ofNullable(orgAcctRestriction).orElseGet(CatalogueItemOrganizationAccountRestrictionEntity::new);
                orgAcctRestrictionToSave.setItemId(itemId);
                orgAcctRestrictionToSave.setOrganizationAccountId(restrictionRequestModel.getOrganizationAccountId());
                catItemOrgAcctRestrictionRepository.save(orgAcctRestrictionToSave);
            } else if (null != orgAcctRestriction) {
                catItemOrgAcctRestrictionRepository.delete(orgAcctRestriction);
            }

            CatalogueItemUserRestrictionEntity userRestriction = savedItem.getCatalogueItemUserRestrictionEntity();
            if (null != restrictionRequestModel.getUserId()) {
                CatalogueItemUserRestrictionEntity userRestrictionToSave = Optional.ofNullable(userRestriction).orElseGet(CatalogueItemUserRestrictionEntity::new);
                userRestrictionToSave.setItemId(itemId);
                userRestrictionToSave.setUserId(restrictionRequestModel.getUserId());
                catItemUserRestrictionRepository.save(userRestrictionToSave);
            } else if (null != userRestriction) {
                catItemUserRestrictionRepository.delete(userRestriction);
            }
        }
    }

    @Transactional
    public void setItemActiveStatus(UUID itemId, ActiveStatusPatchModel requestModel) {
        CatalogueItemEntity existingItem = catalogueItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (requestModel.getActiveStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'activeStatus' cannot be null");
        }

        existingItem.setIsActive(requestModel.getActiveStatus());
        catalogueItemRepository.save(existingItem);
    }

    private Page<CatalogueItemEntity> getAccessibleItems(Integer pageOffset, Integer pageLimit) {
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return catalogueItemRepository.findAll(pageRequest);
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        return catalogueItemRepository.findAccessibleCatalogueItems(userMembership.getOrganizationAccountId(), loggedInUser.getUserId(), pageRequest);
    }

    private void validateItemAccess(CatalogueItemEntity itemEntity) {
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        if (!CatalogueItemAccessUtils.doesUserHaveItemAccess(loggedInUser, itemEntity)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private void validateRequestModel(CatalogueItemRequestModel requestModel) {
        Set<String> errors = new LinkedHashSet<>();
        if (StringUtils.isBlank(requestModel.getName())) {
            errors.add("The field 'Item Name' cannot be blank");
        }

        if (requestModel.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("The field 'Price' cannot be less than $0.00");
        }

        if (requestModel.getQuantity() < 0) {
            errors.add("The field 'Quantity' cannot be less than 0");
        }

        RestrictionModel restriction = requestModel.getRestriction();
        if (restriction != null) {
            if (restriction.getOrganizationAccountId() == null) {
                errors.add("The organizationAccountId cannot be null if this item is restricted");
            } else if (!organizationAccountRepository.existsById(restriction.getOrganizationAccountId())) {
                errors.add("The organizationAccountId specified for the restriction is invalid");
            }

            if (restriction.getUserId() != null && !userRepository.existsById(restriction.getUserId())) {
                errors.add("The userId specified for the restriction is invalid");
            }
        }

        if (!errors.isEmpty()) {
            String combinedErrors = String.join(", ", errors);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("There were errors with the request: %s", combinedErrors));
        }
    }

    // TODO consider sparse response model without restriction object included by default
    private CatalogueItemResponseModel convertToResponseModel(CatalogueItemEntity entity) {
        UUID organizationAccountId = null;
        UUID userId = null;

        CatalogueItemOrganizationAccountRestrictionEntity orgAcctRestriction = entity.getCatalogueItemOrganizationAccountRestrictionEntity();
        if (null != orgAcctRestriction) {
            organizationAccountId = orgAcctRestriction.getOrganizationAccountId();
        }

        CatalogueItemUserRestrictionEntity userRestriction = entity.getCatalogueItemUserRestrictionEntity();
        if (null != userRestriction) {
            userId = userRestriction.getUserId();
        }
        return new CatalogueItemResponseModel(entity.getItemId(), entity.getName(), entity.getPrice(), entity.getQuantity(), entity.getIconId(), entity.getIsActive(), organizationAccountId, userId);
    }

}
