package ai.salesfox.portal.rest.api.inventory.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryOrderProcessingRequestModel {
    private String newStatus;

}
