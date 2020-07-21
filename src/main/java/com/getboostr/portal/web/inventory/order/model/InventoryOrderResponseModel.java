package com.getboostr.portal.web.inventory.order.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class InventoryOrderResponseModel {
    private UUID orderId;
    private UUID organizationAccountId;
    private UUID requestingUserId;
    private UUID inventoryId;
    private UUID itemId;
    private String itemName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private InventoryOrderRequestStatusModel status;

    public InventoryOrderResponseModel(
            UUID orderId,
            UUID organizationAccountId,
            UUID requestingUserId,
            UUID inventoryId,
            UUID itemId,
            String itemName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice,
            UUID changedByUserId,
            OffsetDateTime dateSubmitted,
            OffsetDateTime dateUpdated,
            String processingStatus
    ) {
        this.orderId = orderId;
        this.organizationAccountId = organizationAccountId;
        this.requestingUserId = requestingUserId;
        this.inventoryId = inventoryId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.status = new InventoryOrderRequestStatusModel(changedByUserId, dateSubmitted, dateUpdated, processingStatus);
    }

}
