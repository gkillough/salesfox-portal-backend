package com.getboostr.portal.rest.api.inventory.model;

import com.getboostr.portal.rest.api.common.model.request.RestrictionModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class InventoryResponseModel {
    private UUID inventoryId;
    private RestrictionModel restriction;

    public InventoryResponseModel(UUID inventoryId, UUID organizationAccountId, UUID userId) {
        this.inventoryId = inventoryId;
        this.restriction = new RestrictionModel(organizationAccountId, userId);
    }

}
