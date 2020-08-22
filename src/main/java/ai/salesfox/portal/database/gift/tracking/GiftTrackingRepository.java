package ai.salesfox.portal.database.gift.tracking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface GiftTrackingRepository extends JpaRepository<GiftTrackingEntity, UUID> {

}
