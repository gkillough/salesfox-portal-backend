package ai.salesfox.portal.rest.api.license.type;

import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.license.type.model.LicenseTypeRequestModel;
import ai.salesfox.portal.rest.api.license.type.model.LicenseTypeResponseModel;
import ai.salesfox.portal.rest.api.license.type.model.MultiLicenseTypeModel;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(LicenseTypeController.BASE_ENDPOINT)
public class LicenseTypeController {
    public static final String BASE_ENDPOINT = "/license_types";

    private final LicenseTypeService licenseTypeService;

    @Autowired
    public LicenseTypeController(LicenseTypeService licenseTypeService) {
        this.licenseTypeService = licenseTypeService;
    }

    @GetMapping
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    public MultiLicenseTypeModel getLicenseTypes(
            @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset,
            @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit,
            @RequestParam(required = false) String query
    ) {
        return licenseTypeService.getAllLicenseTypes(offset, limit, query);
    }

    @GetMapping("/{licenseTypeId}")
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    public LicenseTypeResponseModel getLicenseType(@PathVariable UUID licenseTypeId) {
        return licenseTypeService.getLicenseType(licenseTypeId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    public LicenseTypeResponseModel createLicenseType(@RequestBody LicenseTypeRequestModel requestModel) {
        return licenseTypeService.createLicenseType(requestModel);
    }

    @PutMapping("/{licenseTypeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    public void updateLicenseType(@PathVariable UUID licenseTypeId, @RequestBody LicenseTypeRequestModel requestModel) {
        licenseTypeService.updateLicenseType(licenseTypeId, requestModel);
    }

    @DeleteMapping("/{licenseTypeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    public void deleteLicenseType(@PathVariable UUID licenseTypeId) {
        licenseTypeService.deleteLicenseType(licenseTypeId);
    }

}
