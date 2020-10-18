package ai.salesfox.portal.database.campaign;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface UserCampaignSendDateRepository extends JpaRepository<UserCampaignSendDateEntity, UUID> {
}
