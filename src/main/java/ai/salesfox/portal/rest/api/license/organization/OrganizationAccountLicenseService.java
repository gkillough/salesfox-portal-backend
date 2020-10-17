package ai.salesfox.portal.rest.api.license.organization;

import ai.salesfox.portal.database.license.OrganizationAccountLicenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrganizationAccountLicenseService {
    private final OrganizationAccountLicenseRepository orgAcctLicenseRepository;

    @Autowired
    public OrganizationAccountLicenseService(OrganizationAccountLicenseRepository orgAcctLicenseRepository) {
        this.orgAcctLicenseRepository = orgAcctLicenseRepository;
    }

    public Object getLicense(UUID orgAcctId) {
        // TODO validate org account id

        // TODO validate access to org account

        // FIXME implement
        return null;
    }

    public void updateLicense(UUID orgAcctId, Object requestModel) {
        // FIXME implement
    }

}
