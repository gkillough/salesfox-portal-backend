package ai.salesfox.integration.shipstation.model.custom_store.order;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ItemOption {
    private String name;
    private String value;
    private BigDecimal weight;

}
