package ai.salesfox.portal.rest.api.user;

import ai.salesfox.portal.rest.api.user.common.UserEndpointConstants;
import ai.salesfox.portal.rest.api.user.common.model.CurrentUserModel;
import ai.salesfox.portal.rest.api.user.common.model.UserAccountModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // FIXME eventually remove deprecated endpoint
    @GetMapping({
            "/user" + USER_ID_ENDPOINT_PATH_VARIABLE_SUFFIX,
            UserEndpointConstants.BASE_ENDPOINT + USER_ID_ENDPOINT_PATH_VARIABLE_SUFFIX
    })
    public UserAccountModel getUserById(@PathVariable(name = "user_id") UUID userId) {
        return userService.getUser(userId);
    }

}
