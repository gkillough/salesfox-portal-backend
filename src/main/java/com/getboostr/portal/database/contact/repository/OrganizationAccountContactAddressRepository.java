package com.getboostr.portal.database.contact.repository;

import com.getboostr.portal.database.contact.entity.OrganizationAccountContactAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public interface OrganizationAccountContactAddressRepository extends JpaRepository<OrganizationAccountContactAddressEntity, UUID> {
    Optional<OrganizationAccountContactAddressEntity> findByContactId(UUID contactId);

    List<OrganizationAccountContactAddressEntity> findAllByContactIdIn(Collection<UUID> contactId);

}
