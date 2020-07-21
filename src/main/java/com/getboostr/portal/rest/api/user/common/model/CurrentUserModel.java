package com.getboostr.portal.rest.api.user.common.model;

import com.getboostr.portal.rest.security.authorization.PortalAuthorityConstants;
import com.getboostr.portal.rest.api.user.role.model.UserRoleModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserModel {
    private UUID userId;
    private String firstName;
    private String lastName;
    private UserRoleModel role;

    public static final CurrentUserModel ANONYMOUS_USER = new CurrentUserModel(null, PortalAuthorityConstants.ANONYMOUS, PortalAuthorityConstants.ANONYMOUS, UserRoleModel.ANONYMOUS_ROLE);

}
