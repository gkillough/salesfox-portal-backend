package ai.salesfox.portal.database.account.repository;

import ai.salesfox.portal.database.account.entity.LicenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public interface LicenseRepository extends JpaRepository<LicenseEntity, UUID> {
    Optional<LicenseEntity> findFirstByLicenseHash(UUID uuid);

}
