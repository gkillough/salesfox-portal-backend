package com.usepipeline.portal.web.license;

import com.usepipeline.portal.web.common.model.ActiveStatusPatchModel;
import com.usepipeline.portal.web.license.model.LicenseCreationRequestModel;
import com.usepipeline.portal.web.license.model.LicenseModel;
import com.usepipeline.portal.web.license.model.LicenseSeatUpdateModel;
import com.usepipeline.portal.web.license.model.MultiLicenseModel;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(LicenseController.BASE_ENDPOINT)
@PreAuthorize(PortalAuthorityConstants.PIPELINE_ADMIN_OR_ORG_ACCOUNT_OWNER_AUTH_CHECK)
public class LicenseController {
    public static final String BASE_ENDPOINT = "/licenses";

    private LicenseService licenseService;

    @Autowired
    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @GetMapping
    public MultiLicenseModel getAllLicenses() {
        return licenseService.getAllLicenses();
    }

    @GetMapping("/{licenseId}")
    public LicenseModel getLicense(@PathVariable Long licenseId) {
        return licenseService.getLicense(licenseId);
    }

    @PostMapping
    public LicenseModel createLicense(@RequestBody LicenseCreationRequestModel requestModel) {
        return licenseService.createLicense(requestModel);
    }

    @PutMapping("/{licenseId}")
    public void updateLicense(@PathVariable Long licenseId, @RequestBody LicenseCreationRequestModel requestModel) {
        licenseService.updateLicense(licenseId, requestModel);
    }

    @PatchMapping("/{licenseId}/active")
    public void setActiveStatus(@PathVariable Long licenseId, @RequestBody ActiveStatusPatchModel updateModel) {
        licenseService.setActiveStatus(licenseId, updateModel);
    }

    @PatchMapping("/{licenseId}/seats")
    @PreAuthorize(PortalAuthorityConstants.PIPELINE_ADMIN)
    public void setMaxLicenseSeats(@PathVariable Long licenseId, @RequestBody LicenseSeatUpdateModel updateModel) {
        licenseService.setMaxLicenseSeats(licenseId, updateModel);
    }

}
