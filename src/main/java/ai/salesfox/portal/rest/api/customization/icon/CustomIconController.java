package ai.salesfox.portal.rest.api.customization.icon;

import ai.salesfox.portal.rest.api.customization.icon.model.CustomIconRequestModel;
import ai.salesfox.portal.rest.api.customization.icon.model.CustomIconResponseModel;
import ai.salesfox.portal.rest.api.customization.icon.model.MultiCustomIconResponseModel;
import ai.salesfox.portal.rest.api.common.model.request.ActiveStatusPatchModel;
import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.customization.CustomizationEndpointConstants;
import ai.salesfox.portal.rest.api.image.model.ImageResponseModel;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping(CustomIconController.BASE_ENDPOINT)
public class CustomIconController {
    public static final String BASE_ENDPOINT = CustomizationEndpointConstants.BASE_ENDPOINT + "/icons";

    private CustomIconService customIconService;
    private CustomIconImageService customIconImageService;

    @Autowired
    public CustomIconController(CustomIconService customIconService, CustomIconImageService customIconImageService) {
        this.customIconService = customIconService;
        this.customIconImageService = customIconImageService;
    }

    @GetMapping
    public MultiCustomIconResponseModel getCustomIcons(@RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset, @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit) {
        return customIconService.getCustomIcons(offset, limit);
    }

    @GetMapping("/{customIconId}")
    public CustomIconResponseModel getCustomIcon(@PathVariable UUID customIconId) {
        return customIconService.getCustomIcon(customIconId);
    }

    @GetMapping(value = "/{customIconId}/image", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ImageResponseModel getCustomIconImage(@PathVariable UUID customIconId) {
        return customIconImageService.getCustomIconImage(customIconId);
    }

    @PostMapping("/{customIconId}/image")
    @PreAuthorize(PortalAuthorityConstants.NON_ACCOUNT_REP_AUTH_CHECK)
    public void getCustomIconImage(@PathVariable UUID customIconId, @RequestParam MultipartFile customIconFile) {
        customIconImageService.setCustomIconImage(customIconId, customIconFile);
    }

    @PostMapping
    @PreAuthorize(PortalAuthorityConstants.NON_ACCOUNT_REP_AUTH_CHECK)
    public CustomIconResponseModel createCustomIcon(@RequestBody CustomIconRequestModel requestModel) {
        return customIconService.createCustomIcon(requestModel);
    }

    @PutMapping("/{customIconId}")
    @PreAuthorize(PortalAuthorityConstants.NON_ACCOUNT_REP_AUTH_CHECK)
    public void updateCustomIcon(@PathVariable UUID customIconId, @RequestBody CustomIconRequestModel requestModel) {
        customIconService.updateCustomIcon(customIconId, requestModel);
    }

    @PatchMapping("/{customIconId}")
    @PreAuthorize(PortalAuthorityConstants.NON_ACCOUNT_REP_AUTH_CHECK)
    public void setCustomIconActiveStatus(@PathVariable UUID customIconId, @RequestBody ActiveStatusPatchModel requestModel) {
        customIconService.setActiveStatus(customIconId, requestModel);
    }

}
