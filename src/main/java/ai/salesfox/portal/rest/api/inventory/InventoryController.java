package ai.salesfox.portal.rest.api.inventory;

import ai.salesfox.portal.rest.api.inventory.model.InventoryItemResponseModel;
import ai.salesfox.portal.rest.api.inventory.model.InventoryResponseModel;
import ai.salesfox.portal.rest.api.inventory.model.MultiInventoryItemModel;
import ai.salesfox.portal.rest.api.inventory.model.MultiInventoryModel;
import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(InventoryController.BASE_ENDPOINT)
public class InventoryController {
    public static final String BASE_ENDPOINT = "/inventories";

    private InventoryService inventoryService;
    private InventoryItemService inventoryItemService;

    @Autowired
    public InventoryController(InventoryService inventoryService, InventoryItemService inventoryItemService) {
        this.inventoryService = inventoryService;
        this.inventoryItemService = inventoryItemService;
    }

    @GetMapping
    public MultiInventoryModel getInventories(@RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset, @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit) {
        return inventoryService.getInventories(offset, limit);
    }

    @GetMapping("/{inventoryId}")
    public InventoryResponseModel getInventory(@PathVariable UUID inventoryId) {
        return inventoryService.getInventory(inventoryId);
    }

    @GetMapping("/{inventoryId}/items")
    public MultiInventoryItemModel getInventoryItems(@PathVariable UUID inventoryId, @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset, @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit) {
        return inventoryItemService.getInventoryItems(inventoryId, offset, limit);
    }

    @GetMapping("/{inventoryId}/items/{catalogueItemId}")
    public InventoryItemResponseModel getInventoryItem(@PathVariable UUID inventoryId, @PathVariable UUID catalogueItemId) {
        return inventoryItemService.getInventoryItem(inventoryId, catalogueItemId);
    }

    @DeleteMapping("/{inventoryId}/items/{catalogueItemId}")
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_OR_ORG_ACCT_OWNER_OR_ORG_ACCT_MANAGER_AUTH_CHECK)
    public void deleteInventoryItem(@PathVariable UUID inventoryId, @PathVariable UUID catalogueItemId) {
        inventoryItemService.deleteInventoryItem(inventoryId, catalogueItemId);
    }

}
