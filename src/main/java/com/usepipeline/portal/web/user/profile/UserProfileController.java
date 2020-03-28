package com.usepipeline.portal.web.user.profile;

import com.usepipeline.portal.common.exception.PortalRestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(UserProfileController.BASE_ENDPOINT)
public class UserProfileController {
    public static final String BASE_ENDPOINT = "/user_profile";
    private UserProfileService userProfileService;

    @Autowired
    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/{user_id}")
    public UserProfileModel getUserProfile(HttpServletResponse response, @PathVariable(name = "user_id") Long userId) {
        try {
            return userProfileService.getProfile(userId);
        } catch (PortalRestException e) {
            response.setStatus(e.getStatus().value());
        }
        return null;
    }

    @PutMapping("/{user_id}")
    public void updateUserProfile(HttpServletResponse response, @PathVariable(name = "user_id") Long userId, @RequestBody UserProfileModel updateRequestModel) {
        try {
            userProfileService.updateProfile(userId, updateRequestModel);
        } catch (PortalRestException e) {
            response.setStatus(e.getStatus().value());
        }
    }

}
