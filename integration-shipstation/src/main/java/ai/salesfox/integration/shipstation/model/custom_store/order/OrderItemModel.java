package ai.salesfox.integration.shipstation.model.custom_store.order;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class OrderItemModel {
    private String lineItemId;
    private String sku;
    private String name;
    private String imageUrl;
    private BigDecimal weight;
    private String weightUnits; // Pounds, Ounces, Grams
    private Integer quantity;
    private BigDecimal unitPrice;
    private String location;
    private Boolean adjustment;
    private List<ItemOption> options;

}
