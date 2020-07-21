package com.getboostr.portal.database.customization.icon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface CustomIconOwnerRepository extends JpaRepository<CustomIconOwnerEntity, UUID> {

}
