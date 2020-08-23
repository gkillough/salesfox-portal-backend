package ai.salesfox.portal.database.gift.scheduling;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface GiftScheduleRepository extends JpaRepository<GiftScheduleEntity, UUID> {
}
