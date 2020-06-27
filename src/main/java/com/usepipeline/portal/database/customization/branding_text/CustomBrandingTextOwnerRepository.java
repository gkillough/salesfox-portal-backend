package com.usepipeline.portal.database.customization.branding_text;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface CustomBrandingTextOwnerRepository extends JpaRepository<CustomBrandingTextOwnerEntity, CustomBrandingTextOwnerPK> {

}
