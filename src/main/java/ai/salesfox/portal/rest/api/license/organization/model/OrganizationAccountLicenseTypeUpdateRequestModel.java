package ai.salesfox.portal.rest.api.license.organization.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationAccountLicenseTypeUpdateRequestModel {
    private UUID licenseTypeId;

}
