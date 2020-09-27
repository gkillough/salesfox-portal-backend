package ai.salesfox.portal.rest.api.catalogue;

import ai.salesfox.portal.common.FieldValidationUtils;
import ai.salesfox.portal.common.service.catalogue.CatalogueItemAccessUtils;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.account.repository.UserRepository;
import ai.salesfox.portal.database.catalogue.item.CatalogueItemEntity;
import ai.salesfox.portal.database.catalogue.item.CatalogueItemRepository;
import ai.salesfox.portal.database.catalogue.restriction.CatalogueItemOrganizationAccountRestrictionEntity;
import ai.salesfox.portal.database.catalogue.restriction.CatalogueItemOrganizationAccountRestrictionRepository;
import ai.salesfox.portal.database.catalogue.restriction.CatalogueItemUserRestrictionEntity;
import ai.salesfox.portal.database.catalogue.restriction.CatalogueItemUserRestrictionRepository;
import ai.salesfox.portal.database.organization.account.OrganizationAccountRepository;
import ai.salesfox.portal.rest.api.catalogue.model.CatalogueItemRequestModel;
import ai.salesfox.portal.rest.api.catalogue.model.CatalogueItemResponseModel;
import ai.salesfox.portal.rest.api.catalogue.model.MultiCatalogueItemModel;
import ai.salesfox.portal.rest.api.common.model.request.ActiveStatusPatchModel;
import ai.salesfox.portal.rest.api.common.model.request.RestrictionModel;
import ai.salesfox.portal.rest.api.common.page.PageRequestValidationUtils;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Component
public class CatalogueService {
    private final CatalogueItemRepository catalogueItemRepository;
    private final CatalogueItemOrganizationAccountRestrictionRepository catItemOrgAcctRestrictionRepository;
    private final CatalogueItemUserRestrictionRepository catItemUserRestrictionRepository;
    private final OrganizationAccountRepository organizationAccountRepository;
    private final UserRepository userRepository;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public CatalogueService(CatalogueItemRepository catalogueItemRepository, CatalogueItemOrganizationAccountRestrictionRepository catItemOrgAcctRestrictionRepository, CatalogueItemUserRestrictionRepository catItemUserRestrictionRepository,
                            OrganizationAccountRepository organizationAccountRepository, UserRepository userRepository, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.catalogueItemRepository = catalogueItemRepository;
        this.catItemOrgAcctRestrictionRepository = catItemOrgAcctRestrictionRepository;
        this.catItemUserRestrictionRepository = catItemUserRestrictionRepository;
        this.organizationAccountRepository = organizationAccountRepository;
        this.userRepository = userRepository;
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
        CatalogueItemEntity newItem = new CatalogueItemEntity(null, itemRequestModel.getName(), itemRequestModel.getPrice(), itemRequestModel.getShippingCost(), itemRequestModel.getIconUrl(), true);

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

        // FIXME upload to CDN and retrieve url
        String iconUrl = null;
//        File savedIcon = imageUtility.saveImage(iconFile);
//        String savedIconName = FilenameUtils.getName(savedIcon.getName());
//        CatalogueItemIconEntity iconEntityToSave = new CatalogueItemIconEntity(null, savedIconName);
//        CatalogueItemIconEntity savedIconEntity = catalogueItemIconRepository.save(iconEntityToSave);

        exitingItem.setIconUrl(iconUrl);
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
        existingItem.setShippingCost(itemRequestModel.getShippingCost());
        existingItem.setIconUrl(itemRequestModel.getIconUrl());
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
        } else {
            Optional.ofNullable(savedItem.getCatalogueItemOrganizationAccountRestrictionEntity())
                    .ifPresent(catItemOrgAcctRestrictionRepository::delete);
            Optional.ofNullable(savedItem.getCatalogueItemUserRestrictionEntity())
                    .ifPresent(catItemUserRestrictionRepository::delete);
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

        if (null == requestModel.getPrice()) {
            errors.add("The field 'Price' is required");
        } else if (requestModel.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("The field 'Price' cannot be less than $0.00");
        }

        if (null == requestModel.getShippingCost()) {
            errors.add("The field 'Shipping Cost' is required");
        } else if (requestModel.getShippingCost().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("The field 'Shipping Cost' cannot be less than $0.00");
        }

        if (!FieldValidationUtils.isValidUrl(requestModel.getIconUrl(), true)) {
            errors.add("The field 'Icon URL' is invalid");
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
        return new CatalogueItemResponseModel(entity.getItemId(), entity.getName(), entity.getPrice(), entity.getShippingCost(), entity.getIconUrl(), entity.getIsActive(), organizationAccountId, userId);
    }

}
