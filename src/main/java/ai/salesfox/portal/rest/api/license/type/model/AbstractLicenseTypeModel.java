package ai.salesfox.portal.rest.api.license.type.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbstractLicenseTypeModel {
    private String name;
    private Boolean isPublic;
    private BigDecimal monthlyCost;
    private Integer campaignsPerUserPerMonth;
    private Integer contactsPerCampaign;
    private Integer usersIncluded;
    private BigDecimal costPerAdditionalUser;

}
