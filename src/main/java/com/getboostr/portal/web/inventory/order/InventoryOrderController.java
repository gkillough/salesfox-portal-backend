package com.getboostr.portal.web.inventory.order;

import com.getboostr.portal.web.inventory.order.model.InventoryOrderProcessingRequestModel;
import com.getboostr.portal.web.inventory.order.model.InventoryOrderRequestModel;
import com.getboostr.portal.web.inventory.order.model.InventoryOrderResponseModel;
import com.getboostr.portal.web.inventory.order.model.MultiInventoryOrderModel;
import com.getboostr.portal.web.common.page.PageMetadata;
import com.getboostr.portal.web.inventory.InventoryController;
import com.getboostr.portal.web.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(InventoryOrderController.BASE_ENDPOINT)
public class InventoryOrderController {
    public static final String BASE_ENDPOINT = InventoryController.BASE_ENDPOINT + "/orders";

    private InventoryOrderService inventoryOrderService;

    @Autowired
    public InventoryOrderController(InventoryOrderService inventoryOrderService) {
        this.inventoryOrderService = inventoryOrderService;
    }

    @GetMapping
    public MultiInventoryOrderModel getOrders(@RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset, @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit) {
        return inventoryOrderService.getOrders(offset, limit);
    }

    @GetMapping("/{orderId}")
    public InventoryOrderResponseModel getOrder(@PathVariable UUID orderId) {
        return inventoryOrderService.getOrder(orderId);
    }

    @PostMapping
    public InventoryOrderResponseModel submitOrder(@RequestBody InventoryOrderRequestModel requestModel) {
        return inventoryOrderService.submitOrder(requestModel);
    }

    @PostMapping("/{orderId}")
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    public void processOrder(@PathVariable UUID orderId, @RequestBody InventoryOrderProcessingRequestModel requestModel) {
        inventoryOrderService.processOrder(orderId, requestModel);
    }

}
