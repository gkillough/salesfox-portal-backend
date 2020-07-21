package com.getboostr.portal.web.organization.users.model;

import com.getboostr.portal.common.model.PortalAddressModel;
import com.getboostr.portal.web.user.common.model.UserLoginInfoModel;
import com.getboostr.portal.web.user.profile.model.UserProfileModel;
import com.getboostr.portal.web.user.role.model.UserRoleModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationUserAdminViewModel {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private PortalAddressModel address;
    private String mobileNumber;
    private String businessNumber;
    private Boolean isActive;
    private UserRoleModel role;
    private UserLoginInfoModel loginInfo;

    public static OrganizationUserAdminViewModel fromProfile(UUID userId, UserProfileModel profile, Boolean isActive, UserRoleModel role, UserLoginInfoModel loginInfo) {
        return new OrganizationUserAdminViewModel(
                userId,
                profile.getFirstName(),
                profile.getLastName(),
                profile.getEmail(),
                profile.getAddress(),
                profile.getMobileNumber(),
                profile.getBusinessNumber(),
                isActive,
                role,
                loginInfo);
    }

}
