package ai.salesfox.portal.database.campaign;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public interface UserCampaignSendDateRepository extends JpaRepository<UserCampaignSendDateEntity, UUID> {
    @Query("SELECT userCampaign" +
            " FROM UserCampaignSendDateEntity userCampaign" +
            " WHERE userCampaign.userId = :userId" +
            " AND userCampaign.date >= :startDate"
    )
    Page<UserCampaignSendDateEntity> findByUserIdOnOrAfter(@Param("userId") UUID userId, @Param("startDate") LocalDate startDate, Pageable pageable);

}
