package com.usepipeline.portal.database.inventory.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

}