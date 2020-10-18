package ai.salesfox.portal.rest.api.campaign;

import ai.salesfox.portal.rest.api.user.UserEndpointConstants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserCampaignController.BASE_ENDPOINT)
public class UserCampaignController {
    public static final String BASE_ENDPOINT = UserEndpointConstants.BASE_ENDPOINT + "/{userId}/campaigns";

    @GetMapping
    public Object getUserCampaigns() {
        // FIXME implement
        return null;
    }

}
