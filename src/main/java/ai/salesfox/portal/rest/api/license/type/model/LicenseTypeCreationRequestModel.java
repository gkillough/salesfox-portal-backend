package ai.salesfox.portal.rest.api.license.type.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LicenseTypeCreationRequestModel extends AbstractLicenseTypeRequestModel {
    private Integer usersPerTeam;

    public LicenseTypeCreationRequestModel(String name, Boolean isPublic, BigDecimal monthlyCost, Integer campaignsPerUserPerMonth, Integer contactsPerCampaign, Integer usersPerTeam) {
        super(name, isPublic, monthlyCost, campaignsPerUserPerMonth, contactsPerCampaign);
        this.usersPerTeam = usersPerTeam;
    }

}
