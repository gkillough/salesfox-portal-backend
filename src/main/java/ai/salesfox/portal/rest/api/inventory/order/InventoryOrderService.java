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
import ai.salesfox.portal.integration.stripe.StripeService;
import ai.salesfox.portal.rest.api.inventory.InventoryAccessService;
import ai.salesfox.portal.rest.api.inventory.order.model.InventoryOrderRequestModel;
import ai.salesfox.portal.rest.api.inventory.order.model.InventoryOrderResponseModel;
import ai.salesfox.portal.rest.api.inventory.order.model.ItemOrderModel;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import com.stripe.model.Charge;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
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
    private final StripeService stripeService;

    @Autowired
    public InventoryOrderService(CatalogueItemRepository catalogueItemRepository, InventoryRepository inventoryRepository, InventoryItemRepository inventoryItemRepository,
                                 InventoryAccessService inventoryAccessService, HttpSafeUserMembershipRetrievalService membershipRetrievalService, StripeService stripeService) {
        this.catalogueItemRepository = catalogueItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryAccessService = inventoryAccessService;
        this.membershipRetrievalService = membershipRetrievalService;
        this.stripeService = stripeService;
    }

    @Transactional
    // TODO this class and method were gutted to pave the way for "voucher" based inventories
    //  this is a temporary state until we add a payment processing integration
    public InventoryOrderResponseModel submitOrder(InventoryOrderRequestModel requestModel) {
        BigDecimal totalPrice = new BigDecimal(0);
        String stripeChargeToken = requestModel.getStripeChargeToken();
        List<CatalogueItemEntity> catalogueItemEntities = catalogueItemRepository.findAll();
        List<ItemOrderModel> itemOrders = requestModel.getOrders();
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        String userRoleLevel = userMembership.getRoleEntity().getRoleLevel();
        requestModel.getOrders();
        for (ItemOrderModel itemOrder : itemOrders) {
            InventoryEntity foundInventory = findInventoryAndValidateAccess(itemOrder.getCatalogueItemId());
            validateSubmitOrderAccess(userRoleLevel);
            validateOrderRequest(itemOrder);
            for (CatalogueItemEntity item : catalogueItemEntities) {
                if (item.getItemId().equals(itemOrder.getCatalogueItemId())) {
                    validateItemAccess(loggedInUser, item);
                    Integer requestedQuantity = itemOrder.getQuantity();
                    InventoryItemEntity itemToSave = findOrCreateInventoryItemEntity(foundInventory, item);
                    Long newInventoryItemQuantity = Math.addExact(itemToSave.getQuantity(), requestedQuantity);
                    itemToSave.setQuantity(newInventoryItemQuantity);
                    inventoryItemRepository.save(itemToSave);
                    totalPrice.add(item.getPrice());
                    totalPrice.add(item.getShippingCost());
                }
            }

        }

        if (StringUtils.isBlank(stripeChargeToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'stripeChargeToken' is required");
        }

        Charge charge;
        try {
            charge = stripeService.chargeNewCard(stripeChargeToken, totalPrice.intValue());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid card or insufficient funds");
        }

        if (charge == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There was a problem processing the payment");
        }

//        CatalogueItemEntity targetItem = catalogueItemRepository.findById(requestModel.getCatalogueItemId())
//                .filter(CatalogueItemEntity::getIsActive)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No catalogue item with the id [%s] exists", requestModel.getCatalogueItemId())));
//        validateItemAccess(loggedInUser, targetItem);

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

        boolean hasOrganizationManagementMembership = PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER.equals(loggedInUserRoleLevel) || PortalAuthorityConstants.ORGANIZATION_ACCOUNT_MANAGER.equals(loggedInUserRoleLevel);
        if (!hasOrganizationManagementMembership) {
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

    private void validateOrderRequest(ItemOrderModel itemOrder) {
        Set<String> errors = new LinkedHashSet<>();
        if (itemOrder.getCatalogueItemId() == null) {
            errors.add("The field 'catalogueItemId' is required");
        }

        if (itemOrder.getQuantity() == null) {
            errors.add("The field 'quantity' is required");
        } else if (itemOrder.getQuantity() < 1) {
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
