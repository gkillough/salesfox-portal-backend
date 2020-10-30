package ai.salesfox.portal.rest.api.user;

import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.user.common.UserEndpointConstants;
import ai.salesfox.portal.rest.api.user.common.model.CurrentUserModel;
import ai.salesfox.portal.rest.api.user.common.model.MultiUserModel;
import ai.salesfox.portal.rest.api.user.common.model.UserAccountModel;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping
public class UserController {
    public static final String CURRENT_USER_ENDPOINT_SUFFIX = "/current_user";
    public static final String USER_ID_ENDPOINT_PATH_VARIABLE_SUFFIX = "/{user_id}";
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // FIXME eventually remove deprecated endpoint
    @GetMapping({
            "/user" + CURRENT_USER_ENDPOINT_SUFFIX,
            UserEndpointConstants.BASE_ENDPOINT + CURRENT_USER_ENDPOINT_SUFFIX
    })
    public CurrentUserModel getCurrentUser() {
        return userService.getCurrentUserFromSession();
    }

    @GetMapping(UserEndpointConstants.BASE_ENDPOINT)
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    public MultiUserModel getUsers(
            @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset,
            @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit,
            @RequestParam(required = false) String query
    ) {
        return userService.getUsers(offset, limit, query);
    }

    // FIXME eventually remove deprecated endpoint
    @GetMapping({
            "/user" + USER_ID_ENDPOINT_PATH_VARIABLE_SUFFIX,
            UserEndpointConstants.BASE_ENDPOINT + USER_ID_ENDPOINT_PATH_VARIABLE_SUFFIX
    })
    public UserAccountModel getUserById(@PathVariable(name = "user_id") UUID userId) {
        return userService.getUser(userId);
    }

}
