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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public void submitOrder(UUID inventoryID, InventoryOrderRequestModel requestModel) {
        String stripeChargeToken = requestModel.getStripeChargeToken();
        if (StringUtils.isBlank(stripeChargeToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'stripeChargeToken' is required");
        }
        InventoryEntity foundInventory = findInventoryAndValidateAccess(inventoryID);
        BigDecimal totalPrice = new BigDecimal(0);
        List<ItemOrderModel> itemOrders = requestModel.getOrders();
        Map<UUID, ItemOrderModel> itemOrderMap = itemOrders.stream().collect(Collectors.toMap(ItemOrderModel::getCatalogueItemId, Function.identity()));
        List<CatalogueItemEntity> catalogueItemEntities = catalogueItemRepository.findAllById(itemOrderMap.keySet());
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        String userRoleLevel = userMembership.getRoleEntity().getRoleLevel();
        validateSubmitOrderAccess(userRoleLevel);

        for (CatalogueItemEntity item : catalogueItemEntities) {
            ItemOrderModel itemOrder = itemOrderMap.get(item.getItemId());
            validateOrderRequest(itemOrder);
            if (!item.getIsActive()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Catalogue item with the id [%s] unavailable", itemOrder.getCatalogueItemId()));
            } else if (item.getItemId().equals(itemOrder.getCatalogueItemId())) {
                validateItemAccess(loggedInUser, item);
                Integer requestedQuantity = itemOrder.getQuantity();
                InventoryItemEntity itemToSave = findOrCreateInventoryItemEntity(foundInventory, item);
                Long newInventoryItemQuantity = Math.addExact(itemToSave.getQuantity(), requestedQuantity);
                itemToSave.setQuantity(newInventoryItemQuantity);
                inventoryItemRepository.save(itemToSave);
                totalPrice.add(item.getPrice());
                totalPrice.add(item.getShippingCost());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No catalogue item with the id [%s] exists", itemOrder.getCatalogueItemId()));
            }
        }

        Charge charge;
        try {
            charge = stripeService.chargeNewCard(stripeChargeToken, totalPrice.doubleValue());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid card or insufficient funds");
        }

        if (charge == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There was a problem processing the payment");
        }
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
