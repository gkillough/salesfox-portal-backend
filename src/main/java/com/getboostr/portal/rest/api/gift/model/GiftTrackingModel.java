package com.getboostr.portal.rest.api.gift.model;

import com.getboostr.portal.rest.api.user.common.model.UserSummaryModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiftTrackingModel {
    private String status;
    private String distributor;
    private String trackingId;
    private UserSummaryModel updatedByUser;
    private OffsetDateTime dateCreated;
    private OffsetDateTime dateUpdated;

}
