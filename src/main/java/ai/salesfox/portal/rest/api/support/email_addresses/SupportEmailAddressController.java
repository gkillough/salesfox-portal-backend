package ai.salesfox.portal.rest.api.support.email_addresses;

import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.support.email_addresses.model.MultiSupportEmailAddressModel;
import ai.salesfox.portal.rest.api.support.email_addresses.model.SupportEmailAddressRequestModel;
import ai.salesfox.portal.rest.api.support.email_addresses.model.SupportEmailAddressResponseModel;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(SupportEmailAddressController.BASE_ENDPOINT)
@PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
public class SupportEmailAddressController {
    public static final String BASE_ENDPOINT = "/support/email_addresses";

    private final SupportEmailAddressService supportEmailAddressService;

    @Autowired
    public SupportEmailAddressController(SupportEmailAddressService supportEmailAddressService) {
        this.supportEmailAddressService = supportEmailAddressService;
    }

    @GetMapping
    public MultiSupportEmailAddressModel getSupportEmailAddresses(@RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset, @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit) {
        return supportEmailAddressService.getSupportEmailAddresses(offset, limit);
    }

    @GetMapping("/category/{supportEmailCategory}")
    public MultiSupportEmailAddressModel getSupportEmailAddressesByCategory(@PathVariable String supportEmailCategory, @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset, @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit) {
        return supportEmailAddressService.getSupportEmailAddressesByCategory(supportEmailCategory, offset, limit);
    }

    @GetMapping("/{supportEmailId}")
    public SupportEmailAddressResponseModel getSupportEmailAddressesById(@PathVariable UUID supportEmailId) {
        return supportEmailAddressService.getSupportEmailAddressesById(supportEmailId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupportEmailAddressResponseModel createSupportEmailAddress(@RequestBody SupportEmailAddressRequestModel requestModel) {
        return supportEmailAddressService.createSupportEmailAddress(requestModel);
    }

    @PutMapping("/{supportEmailId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateSupportEmailAddresses(@PathVariable UUID supportEmailId, @RequestBody SupportEmailAddressRequestModel requestModel) {
        supportEmailAddressService.updateSupportEmailAddresses(supportEmailId, requestModel);
    }

}