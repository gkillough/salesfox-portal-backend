package ai.salesfox.portal.integration.scribeless.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface GiftScribelessStatusRepository extends JpaRepository<UUID, GiftScribelessStatusEntity> {
}
