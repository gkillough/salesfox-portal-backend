package ai.salesfox.integration.shipstation.model.custom_store.shipnotify;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ShippedItemModel {
    private String lineItemId;
    private String sku;
    private String name;
    private Integer quantity;

}
