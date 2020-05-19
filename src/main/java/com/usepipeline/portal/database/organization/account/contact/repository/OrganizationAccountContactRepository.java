package com.usepipeline.portal.database.organization.account.contact.repository;

import com.usepipeline.portal.database.organization.account.contact.entity.OrganizationAccountContactEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public interface OrganizationAccountContactRepository extends JpaRepository<OrganizationAccountContactEntity, Long> {
    Page<OrganizationAccountContactEntity> findAllByIsActive(boolean isActive, Pageable pageable);

    Page<OrganizationAccountContactEntity> findByOrganizationAccountIdAndIsActive(Long organizationAccountId, boolean isActive, Pageable pageable);

    Page<OrganizationAccountContactEntity> findAllByContactIdInAndIsActive(Collection<Long> contactIds, boolean isActive, Pageable pageable);

}
