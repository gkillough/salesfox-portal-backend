package ai.salesfox.portal.integration.scribeless.api.data;

import ai.salesfox.portal.integration.scribeless.api.ScribelessEndpointConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ScribelessEndpointConstants.BASE_ENDPOINT)
public class ScribelessStyleController {
    private final ScribelessStyleService scribelessStyleService;

    @Autowired
    public ScribelessStyleController(ScribelessStyleService scribelessStyleService) {
        this.scribelessStyleService = scribelessStyleService;
    }

    @GetMapping("/style/options")
    public ScribelessStyleOptionsResponseModel getScribelessDataOptions() {
        return scribelessStyleService.getScribelessStyleOptions();
    }

}
