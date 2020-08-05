package com.getboostr.portal.database.customization.icon.restriction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface CustomIconOrganizationAccountRepository extends JpaRepository<CustomIconOrganizationAccountRestrictionEntity, UUID> {
}
