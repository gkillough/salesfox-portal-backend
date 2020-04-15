package com.usepipeline.portal.web.license;

import com.usepipeline.portal.web.license.model.LicenseCreationRequestModel;
import com.usepipeline.portal.web.license.model.LicenseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(LicenseController.BASE_ENDPOINT)
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

}
