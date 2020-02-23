package com.usepipeline.portal.database.authentication.repository;

import com.usepipeline.portal.database.authentication.entity.OrganizationAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface OrganizationAccountRepository extends JpaRepository<OrganizationAccountEntity, Long> {
    Optional<OrganizationAccountEntity> findByOrganizationAccountName(String organizationAccountName);

}
