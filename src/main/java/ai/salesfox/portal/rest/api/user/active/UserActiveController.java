package ai.salesfox.portal.rest.api.user.active;

import ai.salesfox.portal.rest.api.common.model.request.ActiveStatusPatchModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
    public void updateActiveStatus(@PathVariable UUID userId, @RequestBody ActiveStatusPatchModel updateModel) {
        userActiveService.updateUserActiveStatus(userId, updateModel);
    }

}
