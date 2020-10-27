package ai.salesfox.portal.rest.api.inventory.order;

import ai.salesfox.portal.rest.api.inventory.InventoryController;
import ai.salesfox.portal.rest.api.inventory.order.model.InventoryOrderRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(InventoryController.BASE_ENDPOINT)
public class InventoryOrderController {
    private final InventoryOrderService inventoryOrderService;

    @Autowired
    public InventoryOrderController(InventoryOrderService inventoryOrderService) {
        this.inventoryOrderService = inventoryOrderService;
    }

    @PostMapping("/order")
    public void submitOrder(@RequestBody InventoryOrderRequestModel requestModel) {
        // TODO return response model when this is broken up for payment processing
        inventoryOrderService.submitOrder(requestModel);
    }

    // TODO add a callback for payment processing to confirm an order

}
