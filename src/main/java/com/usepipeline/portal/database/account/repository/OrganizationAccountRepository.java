package com.usepipeline.portal.database.account.repository;

import com.usepipeline.portal.database.account.entity.OrganizationAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface OrganizationAccountRepository extends JpaRepository<OrganizationAccountEntity, Long> {
    Optional<OrganizationAccountEntity> findFirstByOrganizationAccountName(String organizationAccountName);

}
