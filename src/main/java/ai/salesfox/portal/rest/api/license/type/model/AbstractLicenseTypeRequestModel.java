package ai.salesfox.portal.rest.api.license.type.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbstractLicenseTypeRequestModel {
    private String name;
    private BigDecimal monthlyCost;
    private Integer campaignsPerUserPerMonth;
    private Integer contactsPerCampaign;

}
