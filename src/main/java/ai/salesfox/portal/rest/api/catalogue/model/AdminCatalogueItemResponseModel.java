package ai.salesfox.portal.rest.api.catalogue.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminCatalogueItemResponseModel extends CatalogueItemResponseModel {
    private CatalogueItemExternalDetailsModel externalDetails;

    public AdminCatalogueItemResponseModel(UUID itemId, String name, BigDecimal price, BigDecimal shippingCost, String iconUrl, Boolean isActive, UUID organizationAccountId, UUID userId, CatalogueItemExternalDetailsModel externalDetails) {
        super(itemId, name, price, shippingCost, iconUrl, isActive, organizationAccountId, userId);
        this.externalDetails = externalDetails;
    }

}
