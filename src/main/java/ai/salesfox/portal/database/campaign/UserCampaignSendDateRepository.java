package ai.salesfox.portal.database.campaign;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public interface UserCampaignSendDateRepository extends JpaRepository<UserCampaignSendDateEntity, UUID> {
    // TODO consider making this paged if we start creating large licenses
    @Query("SELECT userCampaign" +
            " FROM UserCampaignSendDateEntity userCampaign" +
            " WHERE userCampaign.userId = :userId" +
            " AND userCampaign.date >= :startDate"
    )
    List<UserCampaignSendDateEntity> findByUserIdAfter(@Param("userId") UUID userId, @Param("startDate") LocalDate startDate);

}
