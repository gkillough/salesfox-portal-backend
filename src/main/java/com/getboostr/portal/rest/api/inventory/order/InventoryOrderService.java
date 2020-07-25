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
import com.getboostr.portal.rest.api.common.page.PageRequestValidationUtils;
import com.getboostr.portal.rest.api.inventory.InventoryAccessService;
import com.getboostr.portal.rest.api.inventory.order.model.InventoryOrderProcessingRequestModel;
import com.getboostr.portal.rest.api.inventory.order.model.InventoryOrderRequestModel;
import com.getboostr.portal.rest.api.inventory.order.model.InventoryOrderResponseModel;
import com.getboostr.portal.rest.api.inventory.order.model.MultiInventoryOrderModel;
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
    private final InventoryOrderRequestRepository orderRequestRepository;
    private final InventoryOrderRequestStatusRepository orderRequestStatusRepository;
    private final CatalogueItemRepository catalogueItemRepository;
    private final CatalogueItemRestrictionRepository catalogueItemRestrictionRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryAccessService inventoryAccessService;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public InventoryOrderService(InventoryOrderRequestRepository orderRequestRepository, InventoryOrderRequestStatusRepository orderRequestStatusRepository,
                                 CatalogueItemRepository catalogueItemRepository, CatalogueItemRestrictionRepository catalogueItemRestrictionRepository,
                                 InventoryRepository inventoryRepository, InventoryItemRepository inventoryItemRepository, InventoryAccessService inventoryAccessService, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.orderRequestRepository = orderRequestRepository;
        this.orderRequestStatusRepository = orderRequestStatusRepository;
        this.catalogueItemRepository = catalogueItemRepository;
        this.catalogueItemRestrictionRepository = catalogueItemRestrictionRepository;
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
        Map<UUID, InventoryOrderRequestStatusEntity> orderStatusCache = createIdToEntityCache(() -> orderRequestStatusRepository.findAllByOrderIdIn(relatedOrderStatuses), InventoryOrderRequestStatusEntity::getOrderId);

        List<InventoryOrderResponseModel> responseModels = accessibleOrderRequests
                .stream()
                .map(order -> convertToResponseModel(order, inventoryCache.get(order.getInventoryId()), catalogueItemCache.get(order.getCatalogueItemId()), orderStatusCache.get(order.getOrderId())))
                .collect(Collectors.toList());
        return new MultiInventoryOrderModel(responseModels, accessibleOrderRequests);
    }

    public InventoryOrderResponseModel getOrder(UUID inventoryId, UUID orderId) {
        InventoryEntity foundInventory = findInventoryAndValidateAccess(inventoryId);
        InventoryOrderRequestEntity foundOrder = orderRequestRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        validateReadAccess(foundOrder);

        CatalogueItemEntity targetItem = catalogueItemRepository.findById(foundOrder.getCatalogueItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No catalogue item with the id [%s] exists", foundOrder.getCatalogueItemId())));
        InventoryOrderRequestStatusEntity targetStatus = orderRequestStatusRepository.findByOrderId(foundOrder.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No order request status item with the id [%s] exists", foundOrder.getOrderId())));

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        validateItemAccess(userMembership, targetItem);

        return convertToResponseModel(foundOrder, foundInventory, targetItem, targetStatus);
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
        validateItemAccess(userMembership, targetItem);

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

        String processingStatus = InventoryOrderRequestStatus.SUBMITTED.name();
        OffsetDateTime orderDateTime = PortalDateTimeUtils.getCurrentDateTimeUTC();
        InventoryOrderRequestStatusEntity statusToSave = new InventoryOrderRequestStatusEntity(null, savedOrder.getOrderId(), loggedInUser.getUserId(), processingStatus, orderDateTime, orderDateTime);
        InventoryOrderRequestStatusEntity savedStatus = orderRequestStatusRepository.save(statusToSave);

        return convertToResponseModel(savedOrder, foundInventory, targetItem, savedStatus);
    }

    @Transactional
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
            InventoryItemEntity itemToSave = findOrCreateInventoryItemEntity(foundOrder);
            Long newInventoryItemQuantity = Math.addExact(itemToSave.getQuantity(), foundOrder.getQuantity());
            itemToSave.setQuantity(newInventoryItemQuantity);
            inventoryItemRepository.save(itemToSave);
        }

        orderStatusEntity.setDateUpdated(PortalDateTimeUtils.getCurrentDateTimeUTC());
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

    private void validateReadAccess(InventoryOrderRequestEntity order) {
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return;
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        if (membershipRetrievalService.isAuthenticateUserBasicOrPremiumMember()) {
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

        boolean hasIndividualMembership = isPortalIndividualAccount(loggedInUserRoleLevel);
        boolean hasOrganizationManagementMembership = PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER.equals(loggedInUserRoleLevel) || PortalAuthorityConstants.ORGANIZATION_ACCOUNT_MANAGER.equals(loggedInUserRoleLevel);
        if (!hasIndividualMembership && !hasOrganizationManagementMembership) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private void validateItemAccess(MembershipEntity loggedInUserMembership, CatalogueItemEntity targetItem) {
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return;
        }

        CatalogueItemRestrictionEntity itemRestriction = targetItem.getCatalogueItemRestrictionEntity();
        if (targetItem.getRestricted()
                && (!itemRestriction.getOrganizationAccountId().equals(loggedInUserMembership.getOrganizationAccountId())
                || (itemRestriction.getUserId() != null && !itemRestriction.getUserId().equals(loggedInUserMembership.getUserId())))) {
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
