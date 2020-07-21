package com.getboostr.portal.rest.api.inventory.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryOrderRequestStatusModel {
    private UUID changedByUserId;
    private OffsetDateTime dateSubmitted;
    private OffsetDateTime dateUpdated;
    private String processingStatus;

}
