package com.getboostr.portal.rest.catalogue;

import com.getboostr.portal.common.service.icon.LocalIconManager;
import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.account.repository.UserRepository;
import com.getboostr.portal.database.catalogue.icon.CatalogueItemIconEntity;
import com.getboostr.portal.database.catalogue.icon.CatalogueItemIconRepository;
import com.getboostr.portal.database.catalogue.item.CatalogueItemEntity;
import com.getboostr.portal.database.catalogue.item.CatalogueItemRepository;
import com.getboostr.portal.database.catalogue.restriction.CatalogueItemRestrictionEntity;
import com.getboostr.portal.database.catalogue.restriction.CatalogueItemRestrictionRepository;
import com.getboostr.portal.rest.catalogue.model.CatalogueItemRequestModel;
import com.getboostr.portal.rest.catalogue.model.CatalogueItemResponseModel;
import com.getboostr.portal.rest.catalogue.model.MultiCatalogueItemModel;
import com.getboostr.portal.database.organization.account.OrganizationAccountRepository;
import com.getboostr.portal.rest.common.model.request.ActiveStatusPatchModel;
import com.getboostr.portal.rest.common.model.request.RestrictionModel;
import com.getboostr.portal.rest.common.page.PageRequestValidationUtils;
import com.getboostr.portal.rest.image.HttpSafeImageUtility;
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
    private CatalogueItemRepository catalogueItemRepository;
    private CatalogueItemRestrictionRepository catalogueItemRestrictionRepository;
    private CatalogueItemIconRepository catalogueItemIconRepository;
    private OrganizationAccountRepository organizationAccountRepository;
    private UserRepository userRepository;
    private HttpSafeImageUtility imageUtility;
    private LocalIconManager localIconManager;
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public CatalogueService(CatalogueItemRepository catalogueItemRepository, CatalogueItemRestrictionRepository catalogueItemRestrictionRepository,
                            CatalogueItemIconRepository catalogueItemIconRepository, OrganizationAccountRepository organizationAccountRepository, UserRepository userRepository,
                            HttpSafeImageUtility imageUtility, LocalIconManager localIconManager, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.catalogueItemRepository = catalogueItemRepository;
        this.catalogueItemRestrictionRepository = catalogueItemRestrictionRepository;
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
        if (foundItem.getRestricted()) {
            validateItemAccess(itemId);
        }
        return convertToResponseModel(foundItem);
    }

    @Transactional
    public CatalogueItemResponseModel addItem(CatalogueItemRequestModel itemRequestModel) {
        validateRequestModel(itemRequestModel);

        RestrictionModel restrictionRequestModel = itemRequestModel.getRestriction();
        boolean isRestricted = restrictionRequestModel != null;
        CatalogueItemEntity newItem = new CatalogueItemEntity(null, itemRequestModel.getName(), itemRequestModel.getPrice(), itemRequestModel.getQuantity(), isRestricted, null, true);

        CatalogueItemEntity savedItem = catalogueItemRepository.save(newItem);

        if (isRestricted) {
            CatalogueItemRestrictionEntity newRestriction = new CatalogueItemRestrictionEntity(null, savedItem.getItemId(), restrictionRequestModel.getOrganizationAccountId(), restrictionRequestModel.getUserId());
            catalogueItemRestrictionRepository.save(newRestriction);
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
        existingItem.setRestricted(isRestricted);
        catalogueItemRepository.save(existingItem);

        Optional<CatalogueItemRestrictionEntity> optionalRestriction = catalogueItemRestrictionRepository.findByItemId(itemId);
        if (isRestricted) {
            CatalogueItemRestrictionEntity restrictionToSave = optionalRestriction.orElseGet(CatalogueItemRestrictionEntity::new);
            restrictionToSave.setItemId(itemId);
            restrictionToSave.setOrganizationAccountId(restrictionRequestModel.getOrganizationAccountId());
            restrictionToSave.setUserId(restrictionRequestModel.getUserId());
            catalogueItemRestrictionRepository.save(restrictionToSave);
        } else {
            optionalRestriction.ifPresent(catalogueItemRestrictionRepository::delete);
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
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        return catalogueItemRepository.findAccessibleCatalogueItems(userMembership.getOrganizationAccountId(), loggedInUser.getUserId(), pageRequest);
    }

    private void validateItemAccess(UUID itemId) {
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return;
        }

        Optional<CatalogueItemRestrictionEntity> optionalRestriction = catalogueItemRestrictionRepository.findByItemId(itemId);
        if (optionalRestriction.isPresent()) {
            CatalogueItemRestrictionEntity restriction = optionalRestriction.get();
            UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
            if (restriction.getUserId() != null) {
                if (loggedInUser.getUserId().equals(restriction.getUserId())) {
                    return;
                }
            } else {
                MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
                if (userMembership.getOrganizationAccountId().equals(restriction.getOrganizationAccountId())) {
                    return;
                }
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } else {
            log.error("No restrictions found for item with id [{}] despite the item being restricted", itemId);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot verify access to item");
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
        if (entity.getRestricted()) {
            CatalogueItemRestrictionEntity restrictionEntity = entity.getCatalogueItemRestrictionEntity();
            organizationAccountId = restrictionEntity.getOrganizationAccountId();
            userId = restrictionEntity.getUserId();
        }
        return new CatalogueItemResponseModel(entity.getItemId(), entity.getName(), entity.getPrice(), entity.getQuantity(), entity.getIconId(), entity.getIsActive(), organizationAccountId, userId);
    }

}
