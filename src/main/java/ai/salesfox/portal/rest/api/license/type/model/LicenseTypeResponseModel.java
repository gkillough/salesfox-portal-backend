package ai.salesfox.portal.rest.api.license.type.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenseTypeResponseModel {
    private UUID licenseTypeId;
    private String name;
    private BigDecimal monthlyCost;
    private Integer campaignsPerUserPerMonth;
    private Integer contactsPerCampaign;
    private Integer usersPerTeam;

}
