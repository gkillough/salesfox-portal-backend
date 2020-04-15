package com.usepipeline.portal.web.license;

import com.usepipeline.portal.web.license.model.LicenseCreationRequestModel;
import com.usepipeline.portal.web.license.model.LicenseModel;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(LicenseController.BASE_ENDPOINT)
@PreAuthorize(PortalAuthorityConstants.PIPELINE_ADMIN_ROLE_CHECK)
public class LicenseController {
    public static final String BASE_ENDPOINT = "/license";

    private LicenseService licenseService;

    @Autowired
    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @PostMapping
    public LicenseModel createLicense(@RequestBody LicenseCreationRequestModel requestModel) {
        return licenseService.createLicense(requestModel);
    }

    @PutMapping("/{licenseId}")
    public void updateLicense(@RequestParam Long licenseId, @RequestBody LicenseCreationRequestModel requestModel) {
        licenseService.updateLicense(licenseId, requestModel);
    }

}
