package ai.salesfox.portal.rest.api.note.credit.price.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteCreditPriceResponseModel {
    private BigDecimal noteCreditPrice;

}
