package ai.salesfox.portal.rest.api.organization.invitation.model;

import ai.salesfox.portal.rest.api.user.role.model.UserRoleModel;
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
