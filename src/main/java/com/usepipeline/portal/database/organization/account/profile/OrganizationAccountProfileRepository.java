package com.usepipeline.portal.database.organization.account.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public interface OrganizationAccountProfileRepository extends JpaRepository<OrganizationAccountProfileEntity, UUID> {
    Optional<OrganizationAccountProfileEntity> findFirstByOrganizationAccountId(UUID organizationAccountId);

}
