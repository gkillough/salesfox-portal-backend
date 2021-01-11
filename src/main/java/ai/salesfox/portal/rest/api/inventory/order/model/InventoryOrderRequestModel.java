package ai.salesfox.portal.rest.api.inventory.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryOrderRequestModel {
    private String stripeChargeToken;
    private List<ItemOrderModel> orders;

}
