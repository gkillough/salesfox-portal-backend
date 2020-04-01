package com.usepipeline.portal.database.account.repository;

import com.usepipeline.portal.database.account.entity.LicenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public interface LicenseRepository extends JpaRepository<LicenseEntity, Long> {
    Optional<LicenseEntity> findFirstByLicenseHash(UUID uuid);

}
