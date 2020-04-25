package com.usepipeline.portal.web.user.active;

import com.usepipeline.portal.web.common.model.ActiveStatusPatchModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(UserActiveController.BASE_ENDPOINT)
public class UserActiveController {
    public static final String BASE_ENDPOINT = "/active";
    private UserActiveService userActiveService;

    @Autowired
    public UserActiveController(UserActiveService userActiveService) {
        this.userActiveService = userActiveService;
    }

    @PatchMapping("/{userId}")
    public void updateActiveStatus(@PathVariable Long userId, @RequestBody ActiveStatusPatchModel updateModel) {
        userActiveService.updateUserActiveStatus(userId, updateModel);
    }

}
