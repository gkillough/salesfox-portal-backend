package com.usepipeline.portal.database.authentication.repository;

import com.usepipeline.portal.database.authentication.entity.LicenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface LicenseRepository extends JpaRepository<LicenseEntity, Long> {
}
