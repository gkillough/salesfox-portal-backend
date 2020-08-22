package ai.salesfox.portal.database.inventory.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemPK implements Serializable {
    private UUID catalogueItemId;
    private UUID inventoryId;

}
