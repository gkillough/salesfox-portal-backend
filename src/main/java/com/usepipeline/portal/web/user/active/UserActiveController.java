package com.usepipeline.portal.web.user.active;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserActiveController {
    private UserActiveService userActiveService;

    @Autowired
    public UserActiveController(UserActiveService userActiveService) {
        this.userActiveService = userActiveService;
    }

    @PutMapping("/active/{user_id}")
    public void updateActiveStatus(@PathVariable(name = "user_id") Long userId, @RequestBody UserActiveUpdateModel updateModel) {
        userActiveService.updateUserActiveStatus(userId, updateModel);
    }

}
