package ai.salesfox.portal.rest.api.note.credit.price.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteCreditPriceRequestModel {
    // TODO When we incorporate different note credit price. this will be needed.
    //private UUID noteCreditPriceId;
    private BigDecimal noteCreditPrice;

}
