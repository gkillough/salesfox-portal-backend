package com.getboostr.portal.database.customization.icon.restriction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface CustomIconUserRestrictionRepository extends JpaRepository<CustomIconUserRestrictionEntity, UUID> {

}
