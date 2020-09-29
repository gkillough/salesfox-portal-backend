package ai.salesfox.portal.database.account.repository;

import ai.salesfox.portal.database.account.entity.LicenseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public interface LicenseRepository extends JpaRepository<LicenseEntity, UUID> {
    Optional<LicenseEntity> findFirstByLicenseHash(UUID uuid);

    @Query("SELECT license" +
            " FROM LicenseEntity license" +
            " LEFT JOIN license.organizationAccountEntity orgAcct" +
            " LEFT JOIN orgAcct.organizationEntity org" +
            " WHERE (:query = NULL OR :query = '')" +
            " OR (org = NULL AND orgAcct = NULL)" +
            " OR (org != NULL AND org.organizationName LIKE %:query%)" +
            " OR (orgAcct != NULL AND orgAcct.organizationAccountName LIKE %:query%)"
    )
    Page<LicenseEntity> findLicenseByQuery(@Param("query") String query, Pageable pageable);

}
