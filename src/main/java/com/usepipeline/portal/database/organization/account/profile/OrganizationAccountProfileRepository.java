package com.usepipeline.portal.database.organization.account.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface OrganizationAccountProfileRepository extends JpaRepository<OrganizationAccountProfileEntity, Long> {
    Optional<OrganizationAccountProfileEntity> findFirstByOrganizationAccountId(Long organizationAccountId);

}
