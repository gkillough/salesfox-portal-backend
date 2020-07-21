package com.getboostr.portal.rest.user.common.model;

import com.getboostr.portal.rest.user.role.model.UserRoleModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountModel {
    private String firstName;
    private String lastName;
    private String email;
    private UserRoleModel role;
    private Boolean isActive;

}
