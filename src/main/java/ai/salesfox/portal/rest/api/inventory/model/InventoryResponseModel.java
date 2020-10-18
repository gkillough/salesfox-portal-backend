package ai.salesfox.portal.rest.api.inventory.model;

import ai.salesfox.portal.rest.api.common.model.request.RestrictionModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class InventoryResponseModel {
    private UUID inventoryId;
    private RestrictionModel restriction;

    public InventoryResponseModel(UUID inventoryId, UUID organizationAccountId) {
        this.inventoryId = inventoryId;
        this.restriction = new RestrictionModel(organizationAccountId, null);
    }

}
