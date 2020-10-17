package ai.salesfox.portal.rest.api.license.type.model;

import ai.salesfox.portal.database.license.LicenseTypeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LicenseTypeResponseModel extends AbstractLicenseTypeModel {
    private UUID licenseTypeId;

    public static LicenseTypeResponseModel fromEntity(LicenseTypeEntity licenseTypeEntity) {
        return new LicenseTypeResponseModel(
                licenseTypeEntity.getLicenseTypeId(),
                licenseTypeEntity.getName(),
                licenseTypeEntity.getIsPublic(),
                licenseTypeEntity.getMonthlyCost(),
                licenseTypeEntity.getCampaignsPerUserPerMonth(),
                licenseTypeEntity.getContactsPerCampaign(),
                licenseTypeEntity.getUsersIncluded(),
                licenseTypeEntity.getCostPerAdditionalUser()
        );
    }

    public LicenseTypeResponseModel(UUID licenseTypeId, String name, Boolean isPublic, BigDecimal monthlyCost, Integer campaignsPerUserPerMonth, Integer contactsPerCampaign, Integer usersIncluded, BigDecimal costPerAdditionalUser) {
        super(name, isPublic, monthlyCost, campaignsPerUserPerMonth, contactsPerCampaign, usersIncluded, costPerAdditionalUser);
        this.licenseTypeId = licenseTypeId;
    }

}
