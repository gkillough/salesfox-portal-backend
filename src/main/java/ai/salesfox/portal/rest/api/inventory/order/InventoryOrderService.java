package ai.salesfox.portal.rest.api.inventory.order;

import ai.salesfox.portal.rest.api.inventory.order.model.InventoryOrderProcessingRequestModel;
import ai.salesfox.portal.rest.api.inventory.order.model.InventoryOrderRequestModel;
import ai.salesfox.portal.rest.api.inventory.order.model.InventoryOrderResponseModel;
import ai.salesfox.portal.rest.api.inventory.order.model.MultiInventoryOrderModel;
import ai.salesfox.portal.common.enumeration.InventoryOrderRequestStatus;
import ai.salesfox.portal.common.service.catalogue.CatalogueItemAccessUtils;
import ai.salesfox.portal.common.time.PortalDateTimeUtils;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.catalogue.item.CatalogueItemEntity;
import ai.salesfox.portal.database.catalogue.item.CatalogueItemRepository;
import ai.salesfox.portal.database.inventory.InventoryEntity;
import ai.salesfox.portal.database.inventory.InventoryRepository;
import ai.salesfox.portal.database.inventory.item.InventoryItemEntity;
import ai.salesfox.portal.database.inventory.item.InventoryItemPK;
import ai.salesfox.portal.database.inventory.item.InventoryItemRepository;
import ai.salesfox.portal.database.order.InventoryOrderRequestEntity;
import ai.salesfox.portal.database.order.InventoryOrderRequestRepository;
import ai.salesfox.portal.database.order.status.InventoryOrderRequestStatusEntity;
import ai.salesfox.portal.database.order.status.InventoryOrderRequestStatusRepository;
import ai.salesfox.portal.rest.api.common.page.PageRequestValidationUtils;
import ai.salesfox.portal.rest.api.inventory.InventoryAccessService;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InventoryOrderService {
    private final InventoryOrderRequestRepository orderRequestRepository;
    private final InventoryOrderRequestStatusRepository orderRequestStatusRepository;
    private final CatalogueItemRepository catalogueItemRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryAccessService inventoryAccessService;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public InventoryOrderService(InventoryOrderRequestRepository orderRequestRepository, InventoryOrderRequestStatusRepository orderRequestStatusRepository,
                                 CatalogueItemRepository catalogueItemRepository, InventoryRepository inventoryRepository, InventoryItemRepository inventoryItemRepository,
                                 InventoryAccessService inventoryAccessService, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.orderRequestRepository = orderRequestRepository;
        this.orderRequestStatusRepository = orderRequestStatusRepository;
        this.catalogueItemRepository = catalogueItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryAccessService = inventoryAccessService;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    // TODO consider adding an option to filter by requesting-user
    public MultiInventoryOrderModel getOrders(UUID inventoryId, Integer pageOffset, Integer pageLimit) {
        findInventoryAndValidateAccess(inventoryId);
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);

        Page<InventoryOrderRequestEntity> accessibleOrderRequests = getAccessibleOrderRequests(inventoryId, pageOffset, pageLimit);
        if (accessibleOrderRequests.isEmpty()) {
            return MultiInventoryOrderModel.empty();
        }

        List<InventoryOrderResponseModel> responseModels = accessibleOrderRequests
                .stream()
                .map(this::convertToResponseModel)
                .collect(Collectors.toList());
        return new MultiInventoryOrderModel(responseModels, accessibleOrderRequests);
    }

    public InventoryOrderResponseModel getOrder(UUID inventoryId, UUID orderId) {
        findInventoryAndValidateAccess(inventoryId);
        InventoryOrderRequestEntity foundOrder = orderRequestRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        CatalogueItemEntity targetItem = catalogueItemRepository.findById(foundOrder.getCatalogueItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No catalogue item with the id [%s] exists", foundOrder.getCatalogueItemId())));
        validateReadAccess(foundOrder, targetItem);

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        validateItemAccess(loggedInUser, targetItem);

        return convertToResponseModel(foundOrder);
    }

    @Transactional
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
        validateItemQuantity(targetItem, requestModel.getQuantity());

        InventoryOrderRequestEntity orderToSave = new InventoryOrderRequestEntity(
                null,
                targetItem.getItemId(),
                foundInventory.getInventoryId(),
                userMembership.getOrganizationAccountId(),
                loggedInUser.getUserId(),
                loggedInUser.getUserId(),
                requestModel.getQuantity(),
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
    }

    @Transactional
    // TODO clean this method up
    public void processOrder(UUID inventoryId, UUID orderId, InventoryOrderProcessingRequestModel requestModel) {
        findInventoryAndValidateAccess(inventoryId);
        InventoryOrderRequestEntity foundOrder = orderRequestRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        InventoryOrderRequestStatusEntity orderStatusEntity = orderRequestStatusRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.error("No order request status found for order with order id [{}]", orderId);
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
        if (InventoryOrderRequestStatus.COMPLETED.name().equals(orderStatusEntity.getProcessingStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update the status of a completed order");
        }

        if (StringUtils.isBlank(requestModel.getNewStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'newStatus' cannot be blank");
        } else if (orderStatusEntity.getProcessingStatus().equals(requestModel.getNewStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'newStatus' cannot be the same as the current order status");
        } else if (!EnumUtils.isValidEnum(InventoryOrderRequestStatus.class, requestModel.getNewStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The field 'newStatus' is not a valid status. Acceptable values: %s", Arrays.toString(InventoryOrderRequestStatus.values())));
        }

        InventoryOrderRequestStatus newOrderStatus = InventoryOrderRequestStatus.valueOf(requestModel.getNewStatus());
        if (InventoryOrderRequestStatus.COMPLETED.equals(newOrderStatus)) {
            CatalogueItemEntity orderedItem = foundOrder.getCatalogueItemEntity();
            validateItemQuantity(orderedItem, foundOrder.getQuantity());

            InventoryItemEntity itemToSave = findOrCreateInventoryItemEntity(foundOrder);
            Long newInventoryItemQuantity = Math.addExact(itemToSave.getQuantity(), foundOrder.getQuantity());
            itemToSave.setQuantity(newInventoryItemQuantity);
            inventoryItemRepository.save(itemToSave);

            Long newCatalogItemQuantity = Math.subtractExact(orderedItem.getQuantity(), foundOrder.getQuantity());
            orderedItem.setQuantity(newCatalogItemQuantity);
            catalogueItemRepository.save(orderedItem);
        }

        orderStatusEntity.setDateUpdated(PortalDateTimeUtils.getCurrentDateTime());
        orderStatusEntity.setProcessingStatus(requestModel.getNewStatus());
        orderRequestStatusRepository.save(orderStatusEntity);
    }

    private InventoryEntity findInventoryAndValidateAccess(UUID inventoryId) {
        InventoryEntity foundInventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        inventoryAccessService.validateInventoryAccess(foundInventory);
        return foundInventory;
    }

    private Page<InventoryOrderRequestEntity> getAccessibleOrderRequests(UUID inventoryId, Integer pageOffset, Integer pageLimit) {
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return orderRequestRepository.findAll(pageRequest);
        }
        // TODO should viewing orders be restricted for non org acct owners/managers?
        return orderRequestRepository.findByInventoryId(inventoryId, pageRequest);
    }

    private void validateReadAccess(InventoryOrderRequestEntity order, CatalogueItemEntity catalogueItem) {
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return;
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        if (loggedInUser.getUserId().equals(order.getRequestingUserId())) {
            // The user who created the order should have access to view it, even if the item is restricted now.
            return;
        }

        if (CatalogueItemAccessUtils.doesUserHaveItemAccess(loggedInUser, catalogueItem)) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
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

    private void validateItemAccess(UserEntity userRequestingAccess, CatalogueItemEntity targetItem) {
        if (!CatalogueItemAccessUtils.doesUserHaveItemAccess(userRequestingAccess, targetItem)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private void validateItemQuantity(CatalogueItemEntity catalogueItem, Integer orderQuantity) {
        if (catalogueItem.getQuantity() < orderQuantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested quantity is more than the available quantity");
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

    private InventoryItemEntity findOrCreateInventoryItemEntity(InventoryOrderRequestEntity orderEntity) {
        UUID inventoryId = orderEntity.getInventoryId();
        UUID catalogueItemId = orderEntity.getCatalogueItemId();
        InventoryItemPK inventoryItemPK = new InventoryItemPK(catalogueItemId, inventoryId);

        return inventoryItemRepository.findById(inventoryItemPK)
                .orElseGet(() -> new InventoryItemEntity(orderEntity.getCatalogueItemId(), orderEntity.getInventoryId(), 0L));
    }

    private InventoryOrderResponseModel convertToResponseModel(InventoryOrderRequestEntity entity) {
        BigDecimal bigDecimalQuantity = new BigDecimal(entity.getQuantity());
        BigDecimal totalPrice = entity.getItemPrice().multiply(bigDecimalQuantity);
        InventoryOrderRequestStatusEntity foundOrderStatus = entity.getInventoryOrderRequestStatusEntity();
        return new InventoryOrderResponseModel(
                entity.getOrderId(),
                entity.getOrganizationAccountId(),
                entity.getRequestingUserId(),
                entity.getInventoryId(),
                entity.getCatalogueItemId(),
                entity.getCatalogueItemEntity().getName(),
                entity.getQuantity(),
                entity.getItemPrice(),
                totalPrice,
                foundOrderStatus.getChangedByUserId(),
                foundOrderStatus.getDateSubmitted(),
                foundOrderStatus.getDateUpdated(),
                foundOrderStatus.getProcessingStatus()
        );
    }

}
