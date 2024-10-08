package ai.salesfox.portal.rest.api.catalogue.model;

import ai.salesfox.portal.rest.api.common.model.request.RestrictionModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CatalogueItemResponseModel {
    private UUID itemId;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal shippingCost;
    private String iconUrl;
    private Boolean isActive;
    private RestrictionModel restriction;

    public CatalogueItemResponseModel(UUID itemId, String name, String description, BigDecimal price, BigDecimal shippingCost, String iconUrl, Boolean isActive, UUID organizationAccountId, UUID userId) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.shippingCost = shippingCost;
        this.iconUrl = iconUrl;
        this.isActive = isActive;
        this.restriction = new RestrictionModel(organizationAccountId, userId);
    }

}
