package com.getboostr.portal.web.catalogue;

import com.getboostr.portal.web.catalogue.model.CatalogueItemRequestModel;
import com.getboostr.portal.web.catalogue.model.CatalogueItemResponseModel;
import com.getboostr.portal.web.catalogue.model.MultiCatalogueItemModel;
import com.getboostr.portal.web.common.model.request.ActiveStatusPatchModel;
import com.getboostr.portal.web.common.page.PageMetadata;
import com.getboostr.portal.web.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping(CatalogueController.BASE_ENDPOINT)
public class CatalogueController {
    public static final String BASE_ENDPOINT = "/catalogue/items";

    private CatalogueService catalogueService;

    @Autowired
    public CatalogueController(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    @GetMapping
    public MultiCatalogueItemModel getItems(@RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset, @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit) {
        return catalogueService.getItems(offset, limit);
    }

    @GetMapping("/{itemId}")
    public CatalogueItemResponseModel getItem(@PathVariable UUID itemId) {
        return catalogueService.getItem(itemId);
    }

    @PostMapping
    @PreAuthorize(PortalAuthorityConstants.PIPELINE_ADMIN_AUTH_CHECK)
    public CatalogueItemResponseModel addItem(@RequestBody CatalogueItemRequestModel requestModel) {
        return catalogueService.addItem(requestModel);
    }

    @PostMapping("/{itemId}/icon")
    @PreAuthorize(PortalAuthorityConstants.PIPELINE_ADMIN_AUTH_CHECK)
    public void setItemIcon(@PathVariable UUID itemId, @RequestParam MultipartFile iconFile) {
        catalogueService.setItemIcon(itemId, iconFile);
    }

    @PutMapping("/{itemId}")
    @PreAuthorize(PortalAuthorityConstants.PIPELINE_ADMIN_AUTH_CHECK)
    public void updateItem(@PathVariable UUID itemId, @RequestBody CatalogueItemRequestModel requestModel) {
        catalogueService.updateItem(itemId, requestModel);
    }

    @PatchMapping("/{itemId}/active")
    @PreAuthorize(PortalAuthorityConstants.PIPELINE_ADMIN_AUTH_CHECK)
    public void setItemActiveStatus(@PathVariable UUID itemId, @RequestBody ActiveStatusPatchModel requestModel) {
        catalogueService.setItemActiveStatus(itemId, requestModel);
    }

}
