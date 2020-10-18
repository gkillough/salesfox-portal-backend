package ai.salesfox.portal.rest.api.license.type;

import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.license.type.model.MultiLicenseTypeModel;
import ai.salesfox.portal.rest.security.authentication.AnonymouslyAccessible;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PublicLicenseTypesController.BASE_ENDPOINT)
public class PublicLicenseTypesController implements AnonymouslyAccessible {
    public static final String BASE_ENDPOINT = "/public_license_types";

    private final LicenseTypeService licenseTypeService;

    public PublicLicenseTypesController(LicenseTypeService licenseTypeService) {
        this.licenseTypeService = licenseTypeService;
    }

    @GetMapping
    public MultiLicenseTypeModel getPublicLicenseTypes(
            @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset,
            @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit
    ) {
        return licenseTypeService.getPublicLicenseTypes(offset, limit);
    }

    @Override
    public String[] anonymouslyAccessibleApiAntMatchers() {
        return new String[] {
                PublicLicenseTypesController.BASE_ENDPOINT
        };
    }

}
