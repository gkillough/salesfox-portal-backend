package ai.salesfox.integration.shipstation.model.custom_store.order;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class InsuranceOptionsModel {
    private String provider;
    private boolean insureShipment;
    private BigDecimal insuredValue;

}
