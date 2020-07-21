package com.getboostr.portal.rest.api.catalogue.model;

import com.getboostr.portal.rest.api.common.model.request.RestrictionModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CatalogueItemResponseModel {
    private UUID itemId;
    private String name;
    private BigDecimal price;
    private Long quantity;
    private UUID iconId;
    private Boolean isActive;
    private RestrictionModel restriction;

    public CatalogueItemResponseModel(UUID itemId, String name, BigDecimal price, Long quantity, UUID iconId, Boolean isActive, UUID organizationAccountId, UUID userId) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.iconId = iconId;
        this.isActive = isActive;
        this.restriction = new RestrictionModel(organizationAccountId, userId);
    }

}
