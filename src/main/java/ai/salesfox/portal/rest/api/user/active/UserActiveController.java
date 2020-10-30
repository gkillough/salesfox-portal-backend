package ai.salesfox.portal.rest.api.user.active;

import ai.salesfox.portal.rest.api.common.model.request.ActiveStatusPatchModel;
import ai.salesfox.portal.rest.api.user.common.UserEndpointConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping
public class UserActiveController {
    private final UserActiveService userActiveService;

    @Autowired
    public UserActiveController(UserActiveService userActiveService) {
        this.userActiveService = userActiveService;
    }

    // FIXME eventually remove deprecated endpoint
    @PatchMapping({
            "/active/{userId}",
            UserEndpointConstants.BASE_ENDPOINT + "/{userId}"
    })
    public void updateActiveStatus(@PathVariable UUID userId, @RequestBody ActiveStatusPatchModel updateModel) {
        userActiveService.updateUserActiveStatus(userId, updateModel);
    }

}
