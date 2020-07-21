package com.getboostr.portal.rest.user.profile.model;

import com.getboostr.portal.common.model.PortalAddressModel;
import com.getboostr.portal.rest.user.role.model.UserRoleModel;
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
