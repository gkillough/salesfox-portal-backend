package ai.salesfox.portal.rest.api.registration.organization.model;

import ai.salesfox.portal.common.model.PortalAddressModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationAccountRegistrationModel {
    private String organizationName;
    private String organizationAccountName;
    private UUID licenseTypeId;
    private String businessPhoneNumber;
    private PortalAddressModel organizationAddress;
    private OrganizationAccountUserRegistrationModel accountOwner;

}
