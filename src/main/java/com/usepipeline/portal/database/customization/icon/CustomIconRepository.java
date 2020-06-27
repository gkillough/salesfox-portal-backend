package com.usepipeline.portal.database.customization.icon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface CustomIconRepository extends JpaRepository<CustomIconEntity, UUID> {

}
