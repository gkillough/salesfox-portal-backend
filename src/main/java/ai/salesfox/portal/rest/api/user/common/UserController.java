package ai.salesfox.portal.rest.api.user.common;

import ai.salesfox.portal.rest.api.user.common.model.CurrentUserModel;
import ai.salesfox.portal.rest.api.user.common.model.UserAccountModel;
import ai.salesfox.portal.rest.api.user.UserEndpointConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@RestController
@RequestMapping(UserEndpointConstants.BASE_ENDPOINT)
public class UserController {
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/current_user")
    public CurrentUserModel getCurrentUser() {
        return userService.getCurrentUserFromSession();
    }

    @GetMapping("/{user_id}")
    public UserAccountModel getUserById(HttpServletResponse response, @PathVariable(name = "user_id") UUID userId) {
        return userService.getUser(userId);
    }

}
