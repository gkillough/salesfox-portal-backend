package ai.salesfox.portal.rest.api.inventory.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemOrderModel {
    private UUID catalogueItemId;
    private Integer quantity;

}
