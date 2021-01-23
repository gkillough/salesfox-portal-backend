package ai.salesfox.portal.database.scheduled;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public interface ScheduledTaskRepository extends JpaRepository<ScheduledTaskEntity, UUID> {
    Optional<ScheduledTaskEntity> findByKey(String key);

}
