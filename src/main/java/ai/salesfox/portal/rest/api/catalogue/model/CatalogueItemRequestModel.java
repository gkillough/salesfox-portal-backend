package ai.salesfox.portal.rest.api.catalogue.model;

import ai.salesfox.portal.rest.api.common.model.request.RestrictionModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogueItemRequestModel {
    private String name;
    private BigDecimal price;
    private BigDecimal shippingCost;
    private RestrictionModel restriction;
    private CatalogueItemExternalDetailsModel externalDetails;

}
