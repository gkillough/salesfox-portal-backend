package ai.salesfox.portal.integration.scribeless.api.on_demand;

import ai.salesfox.portal.integration.scribeless.api.ScribelessEndpointConstants;
import ai.salesfox.portal.rest.api.image.model.ImageResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ScribelessEndpointConstants.BASE_ENDPOINT)
public class ScribelessOnDemandController {
    private final ScribelessOnDemandEndpointService onDemandEndpointService;

    @Autowired
    public ScribelessOnDemandController(ScribelessOnDemandEndpointService onDemandEndpointService) {
        this.onDemandEndpointService = onDemandEndpointService;
    }

    @GetMapping(value = "/preview", produces = {MediaType.IMAGE_PNG_VALUE})
    public ImageResponseModel getPreviewImage(
            @RequestParam String text,
            @RequestParam(required = false) Integer widthInMM,
            @RequestParam(required = false) Integer heightInMM,
            @RequestParam(required = false) Integer sizeInMM,
            @RequestParam(required = false) String fontColor,
            @RequestParam(required = false) String handwritingStyle
    ) {
        return onDemandEndpointService.getPreviewImage(text, widthInMM, heightInMM, sizeInMM, fontColor, handwritingStyle);
    }

}
