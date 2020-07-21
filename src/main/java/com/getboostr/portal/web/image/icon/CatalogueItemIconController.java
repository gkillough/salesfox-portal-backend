package com.getboostr.portal.web.image.icon;

import com.getboostr.portal.web.image.model.ImageResponseModel;
import com.getboostr.portal.web.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(CatalogueItemIconController.BASE_ENDPOINT)
public class CatalogueItemIconController {
    // TODO update this endpoint to make a distinction between catalogue icons and custom branding icons
    public static final String BASE_ENDPOINT = "/icons";

    private CatalogueItemIconService iconService;

    @Autowired
    public CatalogueItemIconController(CatalogueItemIconService iconService) {
        this.iconService = iconService;
    }

    @GetMapping(value = "/{iconId}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ImageResponseModel getIcon(@PathVariable UUID iconId) {
        return iconService.getIcon(iconId);
    }

    @DeleteMapping("/{iconId}")
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    public void deleteIcon(@PathVariable UUID iconId) {
        iconService.deleteIcon(iconId);
    }

}
