package ai.salesfox.integration.shipstation.model.custom_store.order;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
