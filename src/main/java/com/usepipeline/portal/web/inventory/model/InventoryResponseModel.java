package com.usepipeline.portal.web.inventory.model;

import com.usepipeline.portal.web.common.model.request.RestrictionModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class InventoryResponseModel {
    private UUID inventoryId;
    private String inventoryName;
    private RestrictionModel restriction;

    public InventoryResponseModel(UUID inventoryId, String inventoryName, UUID organizationAccountId, UUID userId) {
        this.inventoryId = inventoryId;
        this.inventoryName = inventoryName;
        this.restriction = new RestrictionModel(organizationAccountId, userId);
    }

}
