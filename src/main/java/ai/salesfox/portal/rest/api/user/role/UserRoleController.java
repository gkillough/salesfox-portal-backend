package ai.salesfox.portal.rest.api.user.role;

import ai.salesfox.portal.rest.api.user.common.UserEndpointConstants;
import ai.salesfox.portal.rest.api.user.role.model.UserRoleUpdateModel;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping
public class UserRoleController {
    public static final String BASE_ENDPOINT = "/role";
    private final UserRoleService userRoleService;

    @Autowired
    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    @PatchMapping({
            UserRoleController.BASE_ENDPOINT + "/{userId}",
            UserEndpointConstants.BASE_ENDPOINT + "/{userId}/role"
    })
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRole(@PathVariable UUID userId, @RequestBody UserRoleUpdateModel updateModel) {
        userRoleService.updateRole(userId, updateModel);
    }

}
