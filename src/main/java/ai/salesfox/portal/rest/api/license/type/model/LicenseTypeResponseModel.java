package ai.salesfox.portal.rest.api.license.type.model;

import ai.salesfox.portal.database.license.LicenseTypeEntity;
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
    private Boolean isPublic;
    private BigDecimal monthlyCost;
    private Integer campaignsPerUserPerMonth;
    private Integer contactsPerCampaign;
    private Integer usersPerTeam;

    public static LicenseTypeResponseModel fromEntity(LicenseTypeEntity licenseTypeEntity) {
        return new LicenseTypeResponseModel(
                licenseTypeEntity.getLicenseTypeId(),
                licenseTypeEntity.getName(),
                licenseTypeEntity.getIsPublic(),
                licenseTypeEntity.getMonthlyCost(),
                licenseTypeEntity.getCampaignsPerUserPerMonth(),
                licenseTypeEntity.getContactsPerCampaign(),
                licenseTypeEntity.getUsersPerTeam()
        );
    }

}
