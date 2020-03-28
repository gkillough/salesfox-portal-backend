package com.usepipeline.portal.web.user.role;

import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleModel {
    private String level;
    private String description;

    public static final UserRoleModel ANONYMOUS_ROLE = new UserRoleModel(PortalAuthorityConstants.ANONYMOUS, PortalAuthorityConstants.ANONYMOUS);

}
