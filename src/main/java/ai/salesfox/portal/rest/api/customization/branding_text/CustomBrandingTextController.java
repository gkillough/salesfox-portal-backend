package ai.salesfox.portal.rest.api.customization.branding_text;

import ai.salesfox.portal.rest.api.common.model.request.ActiveStatusPatchModel;
import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.customization.CustomizationEndpointConstants;
import ai.salesfox.portal.rest.api.customization.branding_text.model.CustomBrandingTextRequestModel;
import ai.salesfox.portal.rest.api.customization.branding_text.model.CustomBrandingTextResponseModel;
import ai.salesfox.portal.rest.api.customization.branding_text.model.MultiCustomBrandingTextModel;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(CustomBrandingTextController.BASE_ENDPOINT)
public class CustomBrandingTextController {
    public static final String BASE_ENDPOINT = CustomizationEndpointConstants.BASE_ENDPOINT + "/branding_texts";

    private final CustomBrandingTextService customBrandingTextService;

    @Autowired
    public CustomBrandingTextController(CustomBrandingTextService customBrandingTextService) {
        this.customBrandingTextService = customBrandingTextService;
    }

    @GetMapping
    public MultiCustomBrandingTextModel getCustomBrandingTexts(@RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset, @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit) {
        return customBrandingTextService.getCustomBrandingTexts(offset, limit);
    }

    @GetMapping("/{customBrandingTextId}")
    public CustomBrandingTextResponseModel getCustomBrandingText(@PathVariable UUID customBrandingTextId) {
        return customBrandingTextService.getCustomBrandingText(customBrandingTextId);
    }

    @PostMapping
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_OR_ORG_ACCT_OWNER_OR_ORG_ACCT_MANAGER_AUTH_CHECK)
    public CustomBrandingTextResponseModel createCustomBrandingText(@RequestBody CustomBrandingTextRequestModel requestModel) {
        return customBrandingTextService.createCustomBrandingText(requestModel);
    }

    @PutMapping("/{customBrandingTextId}")
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_OR_ORG_ACCT_OWNER_OR_ORG_ACCT_MANAGER_AUTH_CHECK)
    public void updateCustomBrandingText(@PathVariable UUID customBrandingTextId, @RequestBody CustomBrandingTextRequestModel requestModel) {
        customBrandingTextService.updateCustomBrandingText(customBrandingTextId, requestModel);
    }

    @PatchMapping("/{customBrandingTextId}")
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_OR_ORG_ACCT_OWNER_OR_ORG_ACCT_MANAGER_AUTH_CHECK)
    public void setCustomBrandingTextActiveStatus(@PathVariable UUID customBrandingTextId, @RequestBody ActiveStatusPatchModel requestModel) {
        customBrandingTextService.setActiveStatus(customBrandingTextId, requestModel);
    }

}
