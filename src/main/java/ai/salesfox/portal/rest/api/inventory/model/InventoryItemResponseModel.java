package ai.salesfox.portal.rest.api.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemResponseModel {
    private UUID catalogueItemId;
    private UUID inventoryId;
    private String name;
    // TODO should price be listed here if the user has already paid for the voucher?
    private BigDecimal price;
    private Long inventoryQuantity;
    private UUID iconId;

}
