package ai.salesfox.portal.rest.api.license.organization.model;

import ai.salesfox.portal.database.license.OrganizationAccountLicenseEntity;
import ai.salesfox.portal.rest.api.license.type.model.LicenseTypeResponseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationAccountLicenseResponseModel {
    private LicenseTypeResponseModel licenseType;
    private Integer activeUsers;
    private Integer billingDayOfMonth;
    private Boolean isActive;

    public static OrganizationAccountLicenseResponseModel fromEntity(OrganizationAccountLicenseEntity entity) {
        LicenseTypeResponseModel licenseType = LicenseTypeResponseModel.fromEntity(entity.getLicenseTypeEntity());
        return new OrganizationAccountLicenseResponseModel(licenseType, entity.getActiveUsers(), entity.getBillingDayOfMonth(), entity.getIsActive());
    }

}
