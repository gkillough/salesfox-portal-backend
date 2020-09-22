package ai.salesfox.portal.rest.api.inventory.order;

import ai.salesfox.portal.rest.api.inventory.InventoryController;
import ai.salesfox.portal.rest.api.inventory.order.model.InventoryOrderRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/{inventoryId}/order")
    public void submitOrder(@PathVariable UUID inventoryId, @RequestBody InventoryOrderRequestModel requestModel) {
        // TODO return response model when this is broken up for payment processing
        inventoryOrderService.submitOrder(inventoryId, requestModel);
    }

}
