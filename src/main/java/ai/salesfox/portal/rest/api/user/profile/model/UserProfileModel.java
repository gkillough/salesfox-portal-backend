package ai.salesfox.portal.rest.api.user.profile.model;

import ai.salesfox.portal.common.model.PortalAddressModel;
import ai.salesfox.portal.rest.api.user.role.model.UserRoleModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileModel {
    private String firstName;
    private String lastName;
    private String email;
    private PortalAddressModel address;
    private String mobileNumber;
    private String businessNumber;
    private Boolean isActive;
    private UserRoleModel role;

}
