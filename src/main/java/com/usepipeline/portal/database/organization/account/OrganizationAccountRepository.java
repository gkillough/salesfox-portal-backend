package com.usepipeline.portal.database.organization.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface OrganizationAccountRepository extends JpaRepository<OrganizationAccountEntity, Long> {
    Optional<OrganizationAccountEntity> findFirstByOrganizationIdAndOrganizationAccountName(Long organizationId, String organizationAccountName);

    Optional<OrganizationAccountEntity> findFirstByLicenseId(Long licenseId);

}
