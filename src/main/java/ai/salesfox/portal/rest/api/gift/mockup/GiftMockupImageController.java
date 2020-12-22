package ai.salesfox.portal.rest.api.gift.mockup;

import ai.salesfox.portal.rest.api.gift.GiftEndpointConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping(GiftEndpointConstants.BASE_ENDPOINT)
public class GiftMockupImageController {
    private final GiftMockupImageService giftMockupImageService;

    @Autowired
    public GiftMockupImageController(GiftMockupImageService giftMockupImageService) {
        this.giftMockupImageService = giftMockupImageService;
    }

    @PostMapping("/{giftId}/mockup")
    public void uploadMockupImage(@PathVariable UUID giftId, @RequestBody GiftMockupImageRequestModel requestModel) {
        giftMockupImageService.setMockupImage(giftId, requestModel);
    }

    @PostMapping("/{giftId}/mockup/upload")
    public void uploadMockupImage(@PathVariable UUID giftId, @RequestParam MultipartFile mockupImageFile) {
        giftMockupImageService.uploadMockupImage(giftId, mockupImageFile);
    }

}
