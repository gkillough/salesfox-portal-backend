package com.usepipeline.portal.web.user.common;

import com.usepipeline.portal.common.exception.PortalRestException;
import com.usepipeline.portal.web.user.UserEndpointConstants;
import com.usepipeline.portal.web.user.common.model.CurrentUserModel;
import com.usepipeline.portal.web.user.common.model.UserAccountModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

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
    public UserAccountModel getUserById(HttpServletResponse response, @PathVariable(name = "user_id") Long userId) {
        try {
            return userService.getUser(userId);
        } catch (PortalRestException e) {
            response.setStatus(e.getStatus().value());
        }
        return null;
    }


}
