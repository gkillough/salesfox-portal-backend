package ai.salesfox.portal.rest.api.organization.common;

import ai.salesfox.portal.database.organization.OrganizationEntity;
import ai.salesfox.portal.database.organization.OrganizationRepository;
import ai.salesfox.portal.database.organization.account.OrganizationAccountRepository;
import ai.salesfox.portal.rest.api.registration.organization.OrganizationConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class OrganizationValidationService {
    private final OrganizationRepository organizationRepository;
    private final OrganizationAccountRepository organizationAccountRepository;

    @Autowired
    public OrganizationValidationService(OrganizationRepository organizationRepository, OrganizationAccountRepository organizationAccountRepository) {
        this.organizationRepository = organizationRepository;
        this.organizationAccountRepository = organizationAccountRepository;
    }

    public boolean isOrganizationRestricted(String organizationName) {
        return OrganizationConstants.ADMIN_AND_SUPPORT_ORG_NAME.equals(organizationName);
    }

    public boolean isOrganizationAccountNameInUse(String organizationName, String organizationAccountName) {
        Optional<UUID> optionalOrganizationId = organizationRepository.findFirstByOrganizationName(organizationName).map(OrganizationEntity::getOrganizationId);
        if (optionalOrganizationId.isPresent()) {
            return organizationAccountRepository.findFirstByOrganizationIdAndOrganizationAccountName(optionalOrganizationId.get(), organizationAccountName).isPresent();
        } else {
            // If no organization exists yet, then no account names exist yet.
            return false;
        }
    }

    public boolean isOrganizationAccountNameInUse(UUID organizationId, String organizationAccountName) {
        return organizationAccountRepository.findFirstByOrganizationIdAndOrganizationAccountName(organizationId, organizationAccountName).isPresent();
    }

}
