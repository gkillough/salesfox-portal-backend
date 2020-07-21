package com.getboostr.portal.rest.inventory.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryOrderProcessingRequestModel {
    private String newStatus;

}
