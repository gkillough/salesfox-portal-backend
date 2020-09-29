package ai.salesfox.portal.database.catalogue.external;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface CatalogueItemExternalDetailsRepository extends JpaRepository<CatalogueItemExternalDetailsEntity, UUID> {
}
