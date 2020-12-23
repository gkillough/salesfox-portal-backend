package ai.salesfox.portal.database.gift.mockup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface GiftMockupImageRepository extends JpaRepository<GiftMockupImageEntity, UUID> {
}
