package com.getboostr.portal.rest.api.inventory.order;

import com.getboostr.portal.common.enumeration.InventoryOrderRequestStatus;
import com.getboostr.portal.common.time.PortalDateTimeUtils;
import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.catalogue.item.CatalogueItemEntity;
import com.getboostr.portal.database.catalogue.item.CatalogueItemRepository;
import com.getboostr.portal.database.catalogue.restriction.CatalogueItemRestrictionEntity;
import com.getboostr.portal.database.catalogue.restriction.CatalogueItemRestrictionRepository;
import com.getboostr.portal.database.inventory.InventoryEntity;
import com.getboostr.portal.database.inventory.InventoryRepository;
import com.getboostr.portal.database.inventory.item.InventoryItemEntity;
import com.getboostr.portal.database.inventory.item.InventoryItemPK;
import com.getboostr.portal.database.inventory.item.InventoryItemRepository;
import com.getboostr.portal.database.order.InventoryOrderRequestEntity;
import com.getboostr.portal.database.order.InventoryOrderRequestRepository;
import com.getboostr.portal.database.order.status.InventoryOrderRequestStatusEntity;
import com.getboostr.portal.database.order.status.InventoryOrderRequestStatusRepository;
import com.getboostr.portal.rest.api.inventory.order.model.InventoryOrderProcessingRequestModel;
import com.getboostr.portal.rest.api.inventory.order.model.InventoryOrderRequestModel;
import com.getboostr.portal.rest.api.inventory.order.model.InventoryOrderResponseModel;
import com.getboostr.portal.rest.api.inventory.order.model.MultiInventoryOrderModel;
import com.getboostr.portal.rest.api.common.page.PageRequestValidationUtils;
import com.getboostr.portal.rest.security.authorization.PortalAuthorityConstants;
import com.getboostr.portal.rest.util.HttpSafeUserMembershipRetrievalService;
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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InventoryOrderService {
    private InventoryOrderRequestRepository orderRequestRepository;
    private InventoryOrderRequestStatusRepository orderRequestStatusRepository;
    private CatalogueItemRepository catalogueItemRepository;
    private CatalogueItemRestrictionRepository catalogueItemRestrictionRepository;
    private InventoryRepository inventoryRepository;
    private InventoryItemRepository inventoryItemRepository;
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public InventoryOrderService(InventoryOrderRequestRepository orderRequestRepository, InventoryOrderRequestStatusRepository orderRequestStatusRepository,
                                 CatalogueItemRepository catalogueItemRepository, CatalogueItemRestrictionRepository catalogueItemRestrictionRepository,
                                 InventoryRepository inventoryRepository, InventoryItemRepository inventoryItemRepository, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.orderRequestRepository = orderRequestRepository;
        this.orderRequestStatusRepository = orderRequestStatusRepository;
        this.catalogueItemRepository = catalogueItemRepository;
        this.catalogueItemRestrictionRepository = catalogueItemRestrictionRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    // TODO consider adding an option to filter by requesting-user
    public MultiInventoryOrderModel getOrders(Integer pageOffset, Integer pageLimit) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);

        Page<InventoryOrderRequestEntity> accessibleOrderRequests = getAccessibleOrderRequests(pageOffset, pageLimit);
        if (accessibleOrderRequests.isEmpty()) {
            return MultiInventoryOrderModel.empty();
        }

        Set<UUID> relatedInventoryIds = new HashSet<>();
        Set<UUID> relatedCatalogueItemIds = new HashSet<>();
        Set<UUID> relatedOrderStatuses = new HashSet<>();
        for (InventoryOrderRequestEntity order : accessibleOrderRequests) {
            relatedInventoryIds.add(order.getInventoryId());
            relatedCatalogueItemIds.add(order.getCatalogueItemId());
            relatedOrderStatuses.add(order.getOrderId());
        }

        Map<UUID, InventoryEntity> inventoryCache = createIdToEntityCache(() -> inventoryRepository.findAllById(relatedInventoryIds), InventoryEntity::getInventoryId);
        Map<UUID, CatalogueItemEntity> catalogueItemCache = createIdToEntityCache(() -> catalogueItemRepository.findAllById(relatedCatalogueItemIds), CatalogueItemEntity::getItemId);
        Map<UUID, InventoryOrderRequestStatusEntity> orderStatusCache = createIdToEntityCache(() -> orderRequestStatusRepository.findAllByOrderIdIn(relatedOrderStatuses), InventoryOrderRequestStatusEntity::getStatusId);

        List<InventoryOrderResponseModel> responseModels = accessibleOrderRequests
                .stream()
                .map(order -> convertToResponseModel(order, inventoryCache.get(order.getInventoryId()), catalogueItemCache.get(order.getCatalogueItemId()), orderStatusCache.get(order.getOrderId())))
                .collect(Collectors.toList());
        return new MultiInventoryOrderModel(responseModels, accessibleOrderRequests);
    }

    public InventoryOrderResponseModel getOrder(UUID orderId) {
        InventoryOrderRequestEntity foundOrder = orderRequestRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        validateReadAccess(foundOrder);

        InventoryEntity targetInventory = inventoryRepository.findById(foundOrder.getInventoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No inventory with the id [%s] exists", foundOrder.getInventoryId())));
        CatalogueItemEntity targetItem = catalogueItemRepository.findById(foundOrder.getCatalogueItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No catalogue item with the id [%s] exists", foundOrder.getCatalogueItemId())));
        InventoryOrderRequestStatusEntity targetStatus = orderRequestStatusRepository.findByOrderId(foundOrder.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No order request status item with the id [%s] exists", foundOrder.getOrderId())));

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        validateInventoryAndItemAccess(userMembership, targetInventory, targetItem);

        return convertToResponseModel(foundOrder, targetInventory, targetItem, targetStatus);
    }

    @Transactional
    public InventoryOrderResponseModel submitOrder(InventoryOrderRequestModel requestModel) {
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        String userRoleLevel = membershipRetrievalService.getRoleEntity(userMembership).getRoleLevel();
        validateSubmitOrderAccess(userRoleLevel);

        validateOrderRequest(requestModel);

        InventoryEntity targetInventory = inventoryRepository.findById(requestModel.getInventoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No inventory with the id [%s] exists", requestModel.getInventoryId())));
        CatalogueItemEntity targetItem = catalogueItemRepository.findById(requestModel.getCatalogueItemId())
                .filter(CatalogueItemEntity::getIsActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No catalogue item with the id [%s] exists", requestModel.getCatalogueItemId())));
        validateInventoryAndItemAccess(userMembership, targetInventory, targetItem);

        InventoryOrderRequestEntity orderToSave = new InventoryOrderRequestEntity(
                null,
                targetItem.getItemId(),
                targetInventory.getInventoryId(),
                userMembership.getOrganizationAccountId(),
                loggedInUser.getUserId(),
                loggedInUser.getUserId(),
                requestModel.getQuantity(),
                targetItem.getPrice()
        );
        InventoryOrderRequestEntity savedOrder = orderRequestRepository.save(orderToSave);

        String processingStatus = InventoryOrderRequestStatus.SUBMITTED.name();
        OffsetDateTime orderDateTime = PortalDateTimeUtils.getCurrentDateTimeUTC();
        InventoryOrderRequestStatusEntity statusToSave = new InventoryOrderRequestStatusEntity(null, savedOrder.getOrderId(), loggedInUser.getUserId(), processingStatus, orderDateTime, orderDateTime);
        InventoryOrderRequestStatusEntity savedStatus = orderRequestStatusRepository.save(statusToSave);

        return convertToResponseModel(savedOrder, targetInventory, targetItem, savedStatus);
    }

    @Transactional
    public void processOrder(UUID orderId, InventoryOrderProcessingRequestModel requestModel) {
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
            InventoryItemEntity itemToSave = findOrCreateInventoryItemEntity(foundOrder);
            Long newInventoryItemQuantity = Math.addExact(itemToSave.getQuantity(), foundOrder.getQuantity());
            itemToSave.setQuantity(newInventoryItemQuantity);
            inventoryItemRepository.save(itemToSave);
        }

        orderStatusEntity.setDateUpdated(PortalDateTimeUtils.getCurrentDateTimeUTC());
        orderStatusEntity.setProcessingStatus(requestModel.getNewStatus());
        orderRequestStatusRepository.save(orderStatusEntity);
    }

    private Page<InventoryOrderRequestEntity> getAccessibleOrderRequests(Integer pageOffset, Integer pageLimit) {
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return orderRequestRepository.findAll(pageRequest);
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        String roleLevel = membershipRetrievalService.getRoleEntity(userMembership).getRoleLevel();
        if (isPipelineIndividual(roleLevel)) {
            return orderRequestRepository.findByOrganizationAccountIdAndRequestingUserId(userMembership.getOrganizationAccountId(), loggedInUser.getUserId(), pageRequest);
        }
        return orderRequestRepository.findByOrganizationAccountId(userMembership.getOrganizationAccountId(), pageRequest);
    }

    private void validateReadAccess(InventoryOrderRequestEntity order) {
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return;
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        String roleLevel = membershipRetrievalService.getRoleEntity(userMembership).getRoleLevel();
        if (isPipelineIndividual(roleLevel)) {
            if (loggedInUser.getUserId().equals(order.getRequestingUserId())) {
                return;
            }
        } else if (userMembership.getOrganizationAccountId().equals(order.getOrganizationAccountId())) {
            Optional<CatalogueItemRestrictionEntity> optionalItemRestriction = catalogueItemRestrictionRepository.findByItemId(order.getCatalogueItemId());
            if (optionalItemRestriction.isPresent()) {
                CatalogueItemRestrictionEntity restriction = optionalItemRestriction.get();
                if (userMembership.getOrganizationAccountId().equals(restriction.getOrganizationAccountId())
                        && (restriction.getUserId() == null || loggedInUser.getUserId().equals(restriction.getUserId()))) {
                    return;
                }
            } else {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    private void validateSubmitOrderAccess(String loggedInUserRoleLevel) {
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return;
        }

        boolean hasIndividualMembership = isPipelineIndividual(loggedInUserRoleLevel);
        boolean hasOrganizationManagementMembership = PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER.equals(loggedInUserRoleLevel) || PortalAuthorityConstants.ORGANIZATION_ACCOUNT_MANAGER.equals(loggedInUserRoleLevel);
        if (!hasIndividualMembership && !hasOrganizationManagementMembership) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private void validateInventoryAndItemAccess(MembershipEntity loggedInUserMembership, InventoryEntity targetInventory, CatalogueItemEntity targetItem) {
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return;
        }

        CatalogueItemRestrictionEntity itemRestriction = targetItem.getCatalogueItemRestrictionEntity();
        if ((targetInventory.getOrganizationAccountId() != null && !loggedInUserMembership.getOrganizationAccountId().equals(targetInventory.getOrganizationAccountId()))
                || (targetInventory.getUserId() != null && !targetInventory.getUserId().equals(loggedInUserMembership.getUserId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } else if (targetItem.getRestricted()
                && (!itemRestriction.getOrganizationAccountId().equals(loggedInUserMembership.getOrganizationAccountId())
                || (itemRestriction.getUserId() != null && !itemRestriction.getUserId().equals(loggedInUserMembership.getUserId())))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private boolean isPipelineIndividual(String roleLevel) {
        return PortalAuthorityConstants.PORTAL_BASIC_USER.equals(roleLevel) || PortalAuthorityConstants.PORTAL_PREMIUM_USER.equals(roleLevel);
    }

    private void validateOrderRequest(InventoryOrderRequestModel requestModel) {
        Set<String> errors = new LinkedHashSet<>();
        if (requestModel.getInventoryId() == null) {
            errors.add("The field 'inventoryId' is required");
        }

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

    private <T> Map<UUID, T> createIdToEntityCache(Supplier<List<T>> findEntitiesSupplier, Function<T, UUID> keyMapper) {
        return findEntitiesSupplier.get()
                .stream()
                .collect(Collectors.toMap(keyMapper, Function.identity()));
    }

    private InventoryOrderResponseModel convertToResponseModel(InventoryOrderRequestEntity entity, InventoryEntity foundInventory, CatalogueItemEntity foundCatalogueItem, InventoryOrderRequestStatusEntity foundOrderStatus) {
        BigDecimal bigDecimalQuantity = new BigDecimal(entity.getQuantity());
        BigDecimal totalPrice = entity.getItemPrice().multiply(bigDecimalQuantity);
        return new InventoryOrderResponseModel(
                entity.getOrderId(),
                entity.getOrganizationAccountId(),
                entity.getRequestingUserId(),
                foundInventory.getInventoryId(),
                foundCatalogueItem.getItemId(),
                foundCatalogueItem.getName(),
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
