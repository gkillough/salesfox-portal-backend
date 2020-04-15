package com.usepipeline.portal.web.license.model;

import com.usepipeline.portal.common.model.PortalDateModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenseCreationRequestModel {
    private String type;
    private Integer licenseSeats;
    private Double monthlyCost;
    private PortalDateModel expirationDate;

}
