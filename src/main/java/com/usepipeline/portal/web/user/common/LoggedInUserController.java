package com.usepipeline.portal.web.user.common;

import com.usepipeline.portal.web.user.UserEndpointConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserEndpointConstants.BASE_ENDPOINT)
public class LoggedInUserController {
    private LoggedInUserService loggedInUserService;

    @Autowired
    public LoggedInUserController(LoggedInUserService loggedInUserService) {
        this.loggedInUserService = loggedInUserService;
    }

    @GetMapping("/info")
    @PreAuthorize("isAuthenticated()")
    public LoggedInUserModel getUserInfo() {
        return loggedInUserService.getLoggedInUser();
    }

}
