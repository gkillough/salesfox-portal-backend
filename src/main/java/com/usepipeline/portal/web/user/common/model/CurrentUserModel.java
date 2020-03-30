package com.usepipeline.portal.web.user.common.model;

import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import com.usepipeline.portal.web.user.role.model.UserRoleModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserModel {
    private Long userId;
    private String firstName;
    private String lastName;
    private UserRoleModel role;

    public static final CurrentUserModel ANONYMOUS_USER = new CurrentUserModel(null, PortalAuthorityConstants.ANONYMOUS, PortalAuthorityConstants.ANONYMOUS, UserRoleModel.ANONYMOUS_ROLE);

}
