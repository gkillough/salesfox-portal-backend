package ai.salesfox.portal.rest.api.license;

import ai.salesfox.portal.rest.api.common.model.request.ActiveStatusPatchModel;
import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.license.model.LicenseCreationRequestModel;
import ai.salesfox.portal.rest.api.license.model.LicenseModel;
import ai.salesfox.portal.rest.api.license.model.LicenseSeatUpdateModel;
import ai.salesfox.portal.rest.api.license.model.MultiLicenseModel;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(LicenseController.BASE_ENDPOINT)
@PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
@Deprecated
public class LicenseController {
    public static final String BASE_ENDPOINT = "/licenses";

    private final LicenseService licenseService;

    @Autowired
    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @GetMapping
    public MultiLicenseModel getLicenses(
            @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset,
            @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit,
            @RequestParam(required = false) String query
    ) {
        return licenseService.getAllLicenses(offset, limit, query);
    }

    @GetMapping("/{licenseId}")
    public LicenseModel getLicense(@PathVariable UUID licenseId) {
        return licenseService.getLicense(licenseId);
    }

    @PostMapping
    public LicenseModel createLicense(@RequestBody LicenseCreationRequestModel requestModel) {
        return licenseService.createLicense(requestModel);
    }

    @PutMapping("/{licenseId}")
    public void updateLicense(@PathVariable UUID licenseId, @RequestBody LicenseCreationRequestModel requestModel) {
        licenseService.updateLicense(licenseId, requestModel);
    }

    @PatchMapping("/{licenseId}/active")
    public void setActiveStatus(@PathVariable UUID licenseId, @RequestBody ActiveStatusPatchModel updateModel) {
        licenseService.setActiveStatus(licenseId, updateModel);
    }

    @PatchMapping("/{licenseId}/seats")
    public void setMaxLicenseSeats(@PathVariable UUID licenseId, @RequestBody LicenseSeatUpdateModel updateModel) {
        licenseService.setMaxLicenseSeats(licenseId, updateModel);
    }

}
