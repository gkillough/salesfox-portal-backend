package com.usepipeline.portal.database.organization.account.contact.repository;

import com.usepipeline.portal.database.organization.account.contact.entity.OrganizationAccountContactProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public interface OrganizationAccountContactProfileRepository extends JpaRepository<OrganizationAccountContactProfileEntity, Long> {
    Optional<OrganizationAccountContactProfileEntity> findByContactId(Long contactId);

    List<OrganizationAccountContactProfileEntity> findByOrganizationPointOfContactUserId(Long userId);

}
