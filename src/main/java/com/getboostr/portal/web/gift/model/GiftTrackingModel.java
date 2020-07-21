package com.getboostr.portal.web.gift.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiftTrackingModel {
    private String status;
    private String distributor;
    private String trackingId;
    private UUID updatedByUserId;
    private OffsetDateTime dateSubmitted;
    private OffsetDateTime dateUpdated;

}
