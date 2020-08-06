package com.getboostr.portal.database.customization.branding_text.restriction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface CustomBrandingTextOrgAccountRestrictionRepository extends JpaRepository<CustomBrandingTextOrgAccountRestrictionEntity, UUID> {

}
