package com.usepipeline.portal.web.user.active;

import com.usepipeline.portal.web.common.model.ActiveStatusUpdateModel;
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

    @PutMapping("/{user_id}")
    public void updateActiveStatus(@PathVariable(name = "user_id") Long userId, @RequestBody ActiveStatusUpdateModel updateModel) {
        userActiveService.updateUserActiveStatus(userId, updateModel);
    }

}
