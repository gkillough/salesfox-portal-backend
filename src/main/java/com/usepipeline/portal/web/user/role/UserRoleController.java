package com.usepipeline.portal.web.user.role;

import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import com.usepipeline.portal.web.user.role.model.UserRoleUpdateModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(UserRoleController.BASE_ENDPOINT)
public class UserRoleController {
    public static final String BASE_ENDPOINT = "/role";
    private UserRoleService userRoleService;

    @Autowired
    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    @PutMapping("/{user_id}")
    @PreAuthorize(PortalAuthorityConstants.PIPELINE_ADMIN_ROLE_CHECK)
    public void updateRole(@PathVariable(name = "user_id") Long userId, @RequestBody UserRoleUpdateModel updateModel) {
        userRoleService.updateRole(userId, updateModel);
    }

}
