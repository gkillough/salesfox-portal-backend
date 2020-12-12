package ai.salesfox.portal.rest.api.note.credit.price.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteCreditPriceRequestModel {
    private Integer noteCreditPriceId;
    private Double noteCreditPrice;

}
