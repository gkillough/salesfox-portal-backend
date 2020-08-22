package ai.salesfox.portal.rest.api.registration.organization.model;

import ai.salesfox.portal.common.model.PortalAddressModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationAccountUserRegistrationModel {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private PortalAddressModel userAddress;
    private String mobilePhoneNumber;
    private String businessPhoneNumber;

}
