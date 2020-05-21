package com.usepipeline.portal.database.organization.account.contact.repository;

import com.usepipeline.portal.database.organization.account.contact.entity.OrganizationAccountContactAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public interface OrganizationAccountContactAddressRepository extends JpaRepository<OrganizationAccountContactAddressEntity, UUID> {
    Optional<OrganizationAccountContactAddressEntity> findByContactId(UUID contactId);

}
