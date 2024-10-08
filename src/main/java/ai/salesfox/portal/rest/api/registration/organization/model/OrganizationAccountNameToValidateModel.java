package ai.salesfox.portal.rest.api.registration.organization.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationAccountNameToValidateModel {
    private String organizationName;
    private String organizationAccountName;

}
