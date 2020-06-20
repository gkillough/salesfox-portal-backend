package com.usepipeline.portal.web.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemResponseModel {
    private UUID catalogueItemId;
    private UUID inventoryId;
    private String name;
    private BigDecimal price;
    private Long inventoryQuantity;
    private UUID iconId;

}
