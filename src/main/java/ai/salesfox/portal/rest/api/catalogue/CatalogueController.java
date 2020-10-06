package ai.salesfox.portal.rest.api.catalogue;

import ai.salesfox.portal.rest.api.catalogue.model.CatalogueItemRequestModel;
import ai.salesfox.portal.rest.api.catalogue.model.CatalogueItemResponseModel;
import ai.salesfox.portal.rest.api.catalogue.model.MultiCatalogueItemModel;
import ai.salesfox.portal.rest.api.common.model.request.ActiveStatusPatchModel;
import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping(CatalogueController.BASE_ENDPOINT)
public class CatalogueController {
    public static final String BASE_ENDPOINT = "/catalogue/items";

    private final CatalogueService catalogueService;

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
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    public CatalogueItemResponseModel addItem(@RequestBody CatalogueItemRequestModel requestModel) {
        return catalogueService.addItem(requestModel);
    }

    @PostMapping("/{itemId}/icon")
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    public void uploadItemIcon(@PathVariable UUID itemId, @RequestParam MultipartFile iconFile) {
        catalogueService.setItemIcon(itemId, iconFile);
    }

    @PutMapping("/{itemId}")
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    public void updateItem(@PathVariable UUID itemId, @RequestBody CatalogueItemRequestModel requestModel) {
        catalogueService.updateItem(itemId, requestModel);
    }

    @PatchMapping("/{itemId}/active")
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    public void setItemActiveStatus(@PathVariable UUID itemId, @RequestBody ActiveStatusPatchModel requestModel) {
        catalogueService.setItemActiveStatus(itemId, requestModel);
    }

}
