package ai.salesfox.portal.rest.api.license.model;

import ai.salesfox.portal.common.model.PortalDateModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenseCreationRequestModel {
    private String type;
    private Long licenseSeats;
    private BigDecimal monthlyCost;
    private PortalDateModel expirationDate;

}
