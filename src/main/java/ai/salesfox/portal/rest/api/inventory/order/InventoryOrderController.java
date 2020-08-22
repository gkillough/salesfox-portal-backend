package ai.salesfox.portal.rest.api.inventory.order;

import ai.salesfox.portal.rest.api.inventory.order.model.InventoryOrderProcessingRequestModel;
import ai.salesfox.portal.rest.api.inventory.order.model.InventoryOrderRequestModel;
import ai.salesfox.portal.rest.api.inventory.order.model.MultiInventoryOrderModel;
import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.inventory.InventoryController;
import ai.salesfox.portal.rest.api.inventory.order.model.InventoryOrderResponseModel;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(InventoryController.BASE_ENDPOINT)
public class InventoryOrderController {
    private final InventoryOrderService inventoryOrderService;

    @Autowired
    public InventoryOrderController(InventoryOrderService inventoryOrderService) {
        this.inventoryOrderService = inventoryOrderService;
    }

    @GetMapping("/{inventoryId}/orders")
    public MultiInventoryOrderModel getOrders(@PathVariable UUID inventoryId, @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset, @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit) {
        return inventoryOrderService.getOrders(inventoryId, offset, limit);
    }

    @GetMapping("/{inventoryId}/orders/{orderId}")
    public InventoryOrderResponseModel getOrder(@PathVariable UUID inventoryId, @PathVariable UUID orderId) {
        return inventoryOrderService.getOrder(inventoryId, orderId);
    }

    @PostMapping("/{inventoryId}/orders")
    public InventoryOrderResponseModel submitOrder(@PathVariable UUID inventoryId, @RequestBody InventoryOrderRequestModel requestModel) {
        return inventoryOrderService.submitOrder(inventoryId, requestModel);
    }

    @PostMapping("/{inventoryId}/orders/{orderId}")
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    public void processOrder(@PathVariable UUID inventoryId, @PathVariable UUID orderId, @RequestBody InventoryOrderProcessingRequestModel requestModel) {
        inventoryOrderService.processOrder(inventoryId, orderId, requestModel);
    }

}
