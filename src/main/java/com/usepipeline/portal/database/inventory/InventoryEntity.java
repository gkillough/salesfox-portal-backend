package com.usepipeline.portal.database.inventory;

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
@Table(schema = "portal", name = "inventories")
public class InventoryEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "inventory_id")
    private UUID inventoryId;

    @Column(name = "organization_account_id")
    private UUID organizationAccountId;

    @Column(name = "user_id")
    private UUID userId;

}