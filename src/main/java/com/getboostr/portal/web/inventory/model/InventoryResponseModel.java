package com.getboostr.portal.web.inventory.model;

import com.getboostr.portal.web.common.model.request.RestrictionModel;
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
