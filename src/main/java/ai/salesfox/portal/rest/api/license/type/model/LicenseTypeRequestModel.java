package ai.salesfox.portal.rest.api.license.type.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LicenseTypeRequestModel extends AbstractLicenseTypeModel {
    public LicenseTypeRequestModel(String name, Boolean isPublic, BigDecimal monthlyCost, Integer campaignsPerUserPerMonth, Integer contactsPerCampaign, Integer usersIncluded, BigDecimal costPerAdditionalUser, Integer freeTrialDays) {
        super(name, isPublic, monthlyCost, campaignsPerUserPerMonth, contactsPerCampaign, usersIncluded, costPerAdditionalUser, freeTrialDays);
    }

}
