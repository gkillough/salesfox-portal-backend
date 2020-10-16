package ai.salesfox.portal.database.license;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface LicenseTypeRepository extends JpaRepository<LicenseTypeEntity, UUID> {
    boolean existsByName(String name);

    Page<LicenseTypeEntity> findByNameContaining(String name, Pageable pageable);

}
