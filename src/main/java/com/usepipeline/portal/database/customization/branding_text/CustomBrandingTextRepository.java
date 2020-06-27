package com.usepipeline.portal.database.customization.branding_text;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface CustomBrandingTextRepository extends JpaRepository<CustomBrandingTextEntity, UUID> {

}
