package ai.salesfox.portal.integration.scribeless.api;

import ai.salesfox.portal.integration.IntegrationControllerConstants;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ScribelessOnDemandController.BASE_ENDPOINT)
public class ScribelessOnDemandController {
    public static final String BASE_ENDPOINT = IntegrationControllerConstants.BASE_INTEGRATIONS_ENDPOINT + "/scribeless";

}
