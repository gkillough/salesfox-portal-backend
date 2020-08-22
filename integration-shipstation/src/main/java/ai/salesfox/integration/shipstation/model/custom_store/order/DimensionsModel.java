package ai.salesfox.integration.shipstation.model.custom_store.order;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DimensionsModel {
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private String units;

}
