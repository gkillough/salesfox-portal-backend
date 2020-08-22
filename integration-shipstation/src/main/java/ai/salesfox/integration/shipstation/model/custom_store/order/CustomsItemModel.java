package ai.salesfox.integration.shipstation.model.custom_store.order;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CustomsItemModel {
    private String customsItemId;
    private String description;
    private Long quantity;
    private BigDecimal value;
    private String harmonizedTariffCode;
    private String countryOfOrigin;

}
