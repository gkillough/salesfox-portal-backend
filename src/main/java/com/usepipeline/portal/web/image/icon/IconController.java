package com.usepipeline.portal.web.image.icon;

import com.usepipeline.portal.web.image.model.ImageResponseModel;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(IconController.BASE_ENDPOINT)
public class IconController {
    public static final String BASE_ENDPOINT = "/icons";

    private IconService iconService;

    @Autowired
    public IconController(IconService iconService) {
        this.iconService = iconService;
    }

    @GetMapping(value = "/{iconId}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ImageResponseModel getIcon(@PathVariable UUID iconId) {
        return iconService.getIcon(iconId);
    }

    @DeleteMapping("/{iconId}")
    @PreAuthorize(PortalAuthorityConstants.PIPELINE_ADMIN_AUTH_CHECK)
    public void deleteIcon(@PathVariable UUID iconId) {
        iconService.deleteIcon(iconId);
    }

}
