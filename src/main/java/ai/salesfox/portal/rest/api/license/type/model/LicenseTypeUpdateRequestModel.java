package ai.salesfox.portal.rest.api.license.type.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LicenseTypeUpdateRequestModel extends AbstractLicenseTypeRequestModel {
    public LicenseTypeUpdateRequestModel(String name, BigDecimal monthlyCost, Integer campaignsPerUserPerMonth, Integer contactsPerCampaign) {
        super(name, monthlyCost, campaignsPerUserPerMonth, contactsPerCampaign);
    }

}
