package com.getboostr.portal.web.user.profile;

import com.getboostr.portal.web.user.profile.model.UserProfileModel;
import com.getboostr.portal.web.user.profile.model.UserProfileUpdateModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
    public UserProfileModel getUserProfile(@PathVariable(name = "user_id") UUID userId) {
        return userProfileService.retrieveProfile(userId);
    }

    @PutMapping("/{user_id}")
    public void updateUserProfile(@PathVariable(name = "user_id") UUID userId, @RequestBody UserProfileUpdateModel updateRequestModel) {
        userProfileService.updateProfile(userId, updateRequestModel);
    }

}
