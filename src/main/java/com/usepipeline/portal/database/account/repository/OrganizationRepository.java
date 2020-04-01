package com.usepipeline.portal.database.account.repository;

import com.usepipeline.portal.database.account.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {
}
