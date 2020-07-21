package com.getboostr.portal.database.customization.branding_text;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface CustomBrandingTextOwnerRepository extends JpaRepository<CustomBrandingTextOwnerEntity, UUID> {

}
