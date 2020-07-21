package com.getboostr.portal.web.organization.common;

import com.getboostr.portal.database.organization.OrganizationEntity;
import com.getboostr.portal.database.organization.OrganizationRepository;
import com.getboostr.portal.web.registration.organization.OrganizationConstants;
import com.getboostr.portal.database.organization.account.OrganizationAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class OrganizationValidationService {
    private OrganizationRepository organizationRepository;
    private OrganizationAccountRepository organizationAccountRepository;

    @Autowired
    public OrganizationValidationService(OrganizationRepository organizationRepository, OrganizationAccountRepository organizationAccountRepository) {
        this.organizationRepository = organizationRepository;
        this.organizationAccountRepository = organizationAccountRepository;
    }

    public boolean isOrganizationRestricted(String organizationName) {
        return OrganizationConstants.INTERNAL_PIPELINE_ORG_NAME.equals(organizationName) || OrganizationConstants.PLAN_PIPELINE_BASIC_OR_PREMIUM_DEFAULT_ORG_NAME.equals(organizationName);
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
