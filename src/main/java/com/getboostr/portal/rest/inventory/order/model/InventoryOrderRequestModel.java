package com.getboostr.portal.rest.inventory.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryOrderRequestModel {
    private UUID inventoryId;
    private UUID catalogueItemId;
    private Integer quantity;
    
}
