package com.usepipeline.portal.database.customization.icon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface CustomIconOwnerRepository extends JpaRepository<CustomIconOwnerEntity, CustomIconOwnerPK> {

}
