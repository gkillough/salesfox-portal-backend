package com.getboostr.portal.web.customization.icon;

import com.getboostr.portal.web.customization.icon.model.CustomIconRequestModel;
import com.getboostr.portal.web.customization.icon.model.CustomIconResponseModel;
import com.getboostr.portal.web.customization.icon.model.MultiCustomIconResponseModel;
import com.getboostr.portal.web.common.model.request.ActiveStatusPatchModel;
import com.getboostr.portal.web.common.page.PageMetadata;
import com.getboostr.portal.web.customization.CustomizationEndpointConstants;
import com.getboostr.portal.web.image.model.ImageResponseModel;
import com.getboostr.portal.web.security.authorization.PortalAuthorityConstants;
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
