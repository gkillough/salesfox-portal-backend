package com.usepipeline.portal.database.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "order_requests")
public class InventoryOrderRequestEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "order_id")
    private UUID orderId;

    @PrimaryKeyJoinColumn
    @Column(name = "catalogue_item_id")
    private UUID catalogueItemId;

    @PrimaryKeyJoinColumn
    @Column(name = "inventory_id")
    private UUID inventoryId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private UUID organizationAccountId;

    @PrimaryKeyJoinColumn
    @Column(name = "user_id")
    private UUID userId;

    @PrimaryKeyJoinColumn
    @Column(name = "requesting_user_id")
    private UUID requestingUserId;

    @Column(name = "quantity")
    private Integer quantity;

    /**
     * item price on order date
     */
    @Column(name = "item_price")
    private BigDecimal itemPrice;

}