package ai.salesfox.portal.rest.api.inventory.order;

import ai.salesfox.portal.common.service.catalogue.CatalogueItemAccessUtils;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.catalogue.item.CatalogueItemEntity;
import ai.salesfox.portal.database.catalogue.item.CatalogueItemRepository;
import ai.salesfox.portal.database.inventory.InventoryEntity;
import ai.salesfox.portal.database.inventory.InventoryRepository;
import ai.salesfox.portal.database.inventory.item.InventoryItemEntity;
import ai.salesfox.portal.database.inventory.item.InventoryItemPK;
import ai.salesfox.portal.database.inventory.item.InventoryItemRepository;
import ai.salesfox.portal.rest.api.inventory.InventoryAccessService;
import ai.salesfox.portal.rest.api.inventory.order.model.InventoryOrderRequestModel;
import ai.salesfox.portal.rest.api.inventory.order.model.InventoryOrderResponseModel;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class InventoryOrderService {
    private final CatalogueItemRepository catalogueItemRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryAccessService inventoryAccessService;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public InventoryOrderService(CatalogueItemRepository catalogueItemRepository, InventoryRepository inventoryRepository, InventoryItemRepository inventoryItemRepository,
                                 InventoryAccessService inventoryAccessService, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.catalogueItemRepository = catalogueItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryAccessService = inventoryAccessService;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    @Transactional
    // TODO this class and method were gutted to pave the way for "voucher" based inventories
    //  this is a temporary state until we add a payment processing integration
    public InventoryOrderResponseModel submitOrder(UUID inventoryId, InventoryOrderRequestModel requestModel) {
        InventoryEntity foundInventory = findInventoryAndValidateAccess(inventoryId);
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        String userRoleLevel = userMembership.getRoleEntity().getRoleLevel();

        validateSubmitOrderAccess(userRoleLevel);
        validateOrderRequest(requestModel);

        CatalogueItemEntity targetItem = catalogueItemRepository.findById(requestModel.getCatalogueItemId())
                .filter(CatalogueItemEntity::getIsActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No catalogue item with the id [%s] exists", requestModel.getCatalogueItemId())));
        validateItemAccess(loggedInUser, targetItem);

        // TODO insert payment processing in the middle of this step
        /*
        InventoryOrderRequestEntity orderToSave = new InventoryOrderRequestEntity(
                null,
                targetItem.getItemId(),
                foundInventory.getInventoryId(),
                userMembership.getOrganizationAccountId(),
                loggedInUser.getUserId(),
                loggedInUser.getUserId(),
                requestedQuantity,
                targetItem.getPrice()
        );
        InventoryOrderRequestEntity savedOrder = orderRequestRepository.save(orderToSave);

        savedOrder.setCatalogueItemEntity(targetItem);
        savedOrder.setInventoryEntity(foundInventory);

        String processingStatus = InventoryOrderRequestStatus.SUBMITTED.name();
        OffsetDateTime orderDateTime = PortalDateTimeUtils.getCurrentDateTime();
        InventoryOrderRequestStatusEntity statusToSave = new InventoryOrderRequestStatusEntity(null, savedOrder.getOrderId(), loggedInUser.getUserId(), processingStatus, orderDateTime, orderDateTime);
        InventoryOrderRequestStatusEntity savedStatus = orderRequestStatusRepository.save(statusToSave);
        savedOrder.setInventoryOrderRequestStatusEntity(savedStatus);

        return convertToResponseModel(savedOrder);
         */

        Integer requestedQuantity = requestModel.getQuantity();
        InventoryItemEntity itemToSave = findOrCreateInventoryItemEntity(foundInventory, targetItem);
        Long newInventoryItemQuantity = Math.addExact(itemToSave.getQuantity(), requestedQuantity);
        itemToSave.setQuantity(newInventoryItemQuantity);
        inventoryItemRepository.save(itemToSave);

        // TODO fix this when this method is broken up for payment processing
        return null;
    }

    private InventoryEntity findInventoryAndValidateAccess(UUID inventoryId) {
        InventoryEntity foundInventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        inventoryAccessService.validateInventoryAccess(foundInventory);
        return foundInventory;
    }

    private void validateSubmitOrderAccess(String loggedInUserRoleLevel) {
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return;
        }

        boolean hasIndividualMembership = isPortalIndividualAccount(loggedInUserRoleLevel);
        boolean hasOrganizationManagementMembership = PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER.equals(loggedInUserRoleLevel) || PortalAuthorityConstants.ORGANIZATION_ACCOUNT_MANAGER.equals(loggedInUserRoleLevel);
        if (!hasIndividualMembership && !hasOrganizationManagementMembership) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    // TODO this concept may be irrelevant
    //  the original idea was to have certain catalog items that would only be available to certain users/orgs
    private void validateItemAccess(UserEntity userRequestingAccess, CatalogueItemEntity targetItem) {
        if (!CatalogueItemAccessUtils.doesUserHaveItemAccess(userRequestingAccess, targetItem)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private boolean isPortalIndividualAccount(String roleLevel) {
        return PortalAuthorityConstants.PORTAL_BASIC_USER.equals(roleLevel) || PortalAuthorityConstants.PORTAL_PREMIUM_USER.equals(roleLevel);
    }

    private void validateOrderRequest(InventoryOrderRequestModel requestModel) {
        Set<String> errors = new LinkedHashSet<>();
        if (requestModel.getCatalogueItemId() == null) {
            errors.add("The field 'catalogueItemId' is required");
        }

        if (requestModel.getQuantity() == null) {
            errors.add("The field 'quantity' is required");
        } else if (requestModel.getQuantity() < 1) {
            errors.add("The field 'quantity' must be greater than zero");
        }

        if (!errors.isEmpty()) {
            String combinedErrors = String.join(", ", errors);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("There were errors with the request: %s", combinedErrors));
        }
    }

    private InventoryItemEntity findOrCreateInventoryItemEntity(InventoryEntity inventory, CatalogueItemEntity catalogItem) {
        UUID inventoryId = inventory.getInventoryId();
        UUID catalogueItemId = catalogItem.getItemId();
        InventoryItemPK inventoryItemPK = new InventoryItemPK(catalogueItemId, inventoryId);

        return inventoryItemRepository.findById(inventoryItemPK)
                .orElseGet(() -> new InventoryItemEntity(catalogueItemId, inventoryId, 0L));
    }

}
