package ai.salesfox.portal.database.inventory.item;

import ai.salesfox.portal.database.catalogue.item.CatalogueItemEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@IdClass(InventoryItemPK.class)
@Table(schema = "portal", name = "inventory_items")
public class InventoryItemEntity implements Serializable {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "catalogue_item_id")
    private UUID catalogueItemId;

    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "inventory_id")
    private UUID inventoryId;

    @Column(name = "quantity")
    private Long quantity;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "catalogue_item_id", referencedColumnName = "item_id", insertable = false, updatable = false)
    private CatalogueItemEntity catalogueItemEntity;

    public InventoryItemEntity(UUID catalogueItemId, UUID inventoryId, Long quantity) {
        this.catalogueItemId = catalogueItemId;
        this.inventoryId = inventoryId;
        this.quantity = quantity;
    }

}
