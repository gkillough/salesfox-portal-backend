package com.getboostr.portal.database.inventory;

import com.getboostr.portal.database.inventory.restriction.InventoryOrganizationAccountRestrictionEntity;
import com.getboostr.portal.database.inventory.restriction.InventoryUserRestrictionEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "inventories")
public class InventoryEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "inventory_id")
    private UUID inventoryId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "inventory_id", referencedColumnName = "inventory_id", insertable = false, updatable = false)
    private InventoryOrganizationAccountRestrictionEntity inventoryOrganizationAccountRestrictionEntity;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "inventory_id", referencedColumnName = "inventory_id", insertable = false, updatable = false)
    private InventoryUserRestrictionEntity inventoryUserRestrictionEntity;

    public InventoryEntity(UUID inventoryId) {
        this.inventoryId = inventoryId;
    }

    public boolean hasRestriction() {
        return null != inventoryOrganizationAccountRestrictionEntity || null != inventoryUserRestrictionEntity;
    }

}
