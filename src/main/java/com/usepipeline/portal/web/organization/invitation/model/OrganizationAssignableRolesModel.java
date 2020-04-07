package com.usepipeline.portal.web.organization.invitation.model;

import com.usepipeline.portal.web.user.role.model.UserRoleModel;
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
