package ai.salesfox.portal.database.catalogue.icon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface CatalogueItemIconRepository extends JpaRepository<CatalogueItemIconEntity, UUID> {

}
