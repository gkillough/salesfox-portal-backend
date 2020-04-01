package com.usepipeline.portal.database.account.repository;

import com.usepipeline.portal.database.account.entity.OrganizationAccountProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface OrganizationAccountProfileRepository extends JpaRepository<OrganizationAccountProfileEntity, Long> {
    Optional<OrganizationAccountProfileEntity> findFirstByOrganizationAccountId(Long organizationAccountId);

}
