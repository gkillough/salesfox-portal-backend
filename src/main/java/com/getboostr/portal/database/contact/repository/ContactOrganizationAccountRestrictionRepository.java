package com.getboostr.portal.database.contact.repository;

import com.getboostr.portal.database.contact.entity.ContactOrganizationAccountRestrictionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface ContactOrganizationAccountRestrictionRepository extends JpaRepository<ContactOrganizationAccountRestrictionEntity, UUID> {
}
