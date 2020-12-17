package ai.salesfox.portal.rest.api.support.email_addresses;

import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.support.email_addresses.model.MultiSupportEmailAddressModel;
import ai.salesfox.portal.rest.api.support.email_addresses.model.SupportEmailAddressesRequestModel;
import ai.salesfox.portal.rest.api.support.email_addresses.model.SupportEmailAddressesResponseModel;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(SupportEmailAddressesController.BASE_ENDPOINT)
public class SupportEmailAddressesController {
    public static final String BASE_ENDPOINT = "/support/email_addresses";

    private final SupportEmailAddressesService supportEmailAddressesService;

    @Autowired
    public SupportEmailAddressController(SupportEmailAddressesService supportEmailAddressesService) {
        this.supportEmailAddressesService = supportEmailAddressesService;
    }

    @GetMapping
    public MultiSupportEmailAddressModel getSupportEmailAddresses(@RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset, @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit) {
        return supportEmailAddressesService.getSupportEmailAddresses(offset, limit);
    }

    @GetMapping("/{supportEmailId}")
    public SupportEmailAddressesResponseModel getSupportEmailAddressesById(@PathVariable UUID supportEmailId) {
        return supportEmailAddressesService.getSupportEmailAddressesById(supportEmailId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupportEmailAddressesResponseModel createSupportEmailAddress(@PathVariable UUID supportEmailId, @RequestBody SupportEmailAddressesRequestModel requestModel) {
        return supportEmailAddressesService.createSupportEmailAddress(requestModel);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    public void updateSupportEmailAddresses(@PathVariable UUID supportEmailId, @RequestBody SupportEmailAddressesRequestModel requestModel) {
        supportEmailAddressesService.updateSupportEmailAddresses(supportEmailId, requestModel);
    }

}