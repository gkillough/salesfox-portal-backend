package com.usepipeline.portal.database.organization.account.contact.repository;

import com.usepipeline.portal.database.organization.account.contact.entity.OrganizationAccountContactProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public interface OrganizationAccountContactProfileRepository extends JpaRepository<OrganizationAccountContactProfileEntity, UUID> {
    Optional<OrganizationAccountContactProfileEntity> findByContactId(UUID contactId);

    List<OrganizationAccountContactProfileEntity> findAllByContactIdIn(Collection<UUID> contactIds);

    List<OrganizationAccountContactProfileEntity> findByOrganizationPointOfContactUserId(UUID userId);

}
