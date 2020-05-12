package com.usepipeline.portal.database.organization.account.contact.repository;

import com.usepipeline.portal.database.organization.account.contact.entity.OrganizationAccountContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public interface OrganizationAccountContactRepository extends JpaRepository<OrganizationAccountContactEntity, Long> {
    List<OrganizationAccountContactEntity> findByOrganizationAccountIdAndIsActive(Long organizationAccountId, boolean isActive);

    List<OrganizationAccountContactEntity> findAllByContactIdInAndIsActive(Collection<Long> contactIds, boolean isActive);

}
