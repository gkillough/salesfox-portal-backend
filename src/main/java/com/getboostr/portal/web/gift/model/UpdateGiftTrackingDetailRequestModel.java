package com.getboostr.portal.web.gift.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGiftTrackingDetailRequestModel {
    private String distributor;
    private String trackingId;

}
