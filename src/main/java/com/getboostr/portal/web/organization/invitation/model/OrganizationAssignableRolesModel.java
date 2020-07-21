package com.getboostr.portal.web.organization.invitation.model;

import com.getboostr.portal.web.user.role.model.UserRoleModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationAssignableRolesModel {
    private List<UserRoleModel> roles;

}
