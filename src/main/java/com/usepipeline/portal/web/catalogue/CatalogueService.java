package com.usepipeline.portal.web.catalogue;

import com.usepipeline.portal.common.exception.PortalFileSystemException;
import com.usepipeline.portal.common.service.icon.LocalIconManager;
import com.usepipeline.portal.database.account.entity.MembershipEntity;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.account.repository.UserRepository;
import com.usepipeline.portal.database.catalogue.icon.CatalogueItemIconEntity;
import com.usepipeline.portal.database.catalogue.icon.CatalogueItemIconRepository;
import com.usepipeline.portal.database.catalogue.item.CatalogueItemEntity;
import com.usepipeline.portal.database.catalogue.item.CatalogueItemRepository;
import com.usepipeline.portal.database.catalogue.restriction.CatalogueItemRestrictionEntity;
import com.usepipeline.portal.database.catalogue.restriction.CatalogueItemRestrictionRepository;
import com.usepipeline.portal.database.organization.account.OrganizationAccountRepository;
import com.usepipeline.portal.web.catalogue.model.CatalogueItemRequestModel;
import com.usepipeline.portal.web.catalogue.model.CatalogueItemResponseModel;
import com.usepipeline.portal.web.catalogue.model.MultiCatalogueItemModel;
import com.usepipeline.portal.web.common.model.request.RestrictionModel;
import com.usepipeline.portal.web.common.page.PageRequestValidationUtils;
import com.usepipeline.portal.web.util.HttpSafeUserMembershipRetrievalService;
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
import java.io.IOException;
import java.io.InputStream;
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
    private LocalIconManager localIconManager;
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public CatalogueService(CatalogueItemRepository catalogueItemRepository, CatalogueItemRestrictionRepository catalogueItemRestrictionRepository,
                            CatalogueItemIconRepository catalogueItemIconRepository, OrganizationAccountRepository organizationAccountRepository, UserRepository userRepository,
                            LocalIconManager localIconManager, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.catalogueItemRepository = catalogueItemRepository;
        this.catalogueItemRestrictionRepository = catalogueItemRestrictionRepository;
        this.catalogueItemIconRepository = catalogueItemIconRepository;
        this.organizationAccountRepository = organizationAccountRepository;
        this.userRepository = userRepository;
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
        CatalogueItemEntity newItem = new CatalogueItemEntity(null, itemRequestModel.getName(), itemRequestModel.getPrice(), itemRequestModel.getQuantity(), isRestricted, null);

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

        if (iconFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The provided file cannot be empty");
        }

        File savedIcon;
        try (InputStream fileInputStream = iconFile.getInputStream()) {
            String iconFileExtension = FilenameUtils.getExtension(iconFile.getOriginalFilename());
            if (StringUtils.isBlank(iconFileExtension)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The file extension cannot be blank");
            }

            savedIcon = localIconManager.saveIcon(fileInputStream, iconFileExtension);
        } catch (IOException | PortalFileSystemException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The provided file was an invalid image");
        }

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
    public void deleteItem(UUID itemId) {
        CatalogueItemEntity existingItem = catalogueItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (existingItem.getQuantity() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete catalogue item if quantity is not equal to zero");
        }
        catalogueItemRepository.deleteById(itemId);
    }

    private Page<CatalogueItemEntity> getAccessibleItems(Integer pageOffset, Integer pageLimit) {
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        if (membershipRetrievalService.isAuthenticatedUserPipelineAdmin()) {
            return catalogueItemRepository.findAll(pageRequest);
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        return catalogueItemRepository.findAccessibleCatalogueItems(userMembership.getOrganizationAccountId(), loggedInUser.getUserId(), pageRequest);
    }

    private void validateItemAccess(UUID itemId) {
        if (membershipRetrievalService.isAuthenticatedUserPipelineAdmin()) {
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

        if (requestModel.getPrice().compareTo(BigDecimal.ZERO) >= 0) {
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

    private CatalogueItemResponseModel convertToResponseModel(CatalogueItemEntity entity) {
        UUID organizationAccountId = null;
        UUID userId = null;
        if (entity.getRestricted()) {
            CatalogueItemRestrictionEntity restrictionEntity = entity.getCatalogueItemRestrictionEntity();
            organizationAccountId = restrictionEntity.getOrganizationAccountId();
            userId = restrictionEntity.getUserId();
        }
        return new CatalogueItemResponseModel(entity.getItemId(), entity.getName(), entity.getPrice(), entity.getQuantity(), entity.getIconId(), organizationAccountId, userId);
    }

}
