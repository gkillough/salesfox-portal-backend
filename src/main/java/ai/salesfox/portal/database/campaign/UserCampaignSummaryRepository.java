package ai.salesfox.portal.database.campaign;

import ai.salesfox.portal.database.campaign.view.CampaignDateSummaryView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public interface UserCampaignSummaryRepository extends JpaRepository<UserCampaignSummaryEntity, UUID> {
    @Query("SELECT userCampaign" +
            " FROM UserCampaignSummaryEntity userCampaign" +
            " WHERE userCampaign.userId = :userId" +
            " AND userCampaign.date >= :startDate"
    )
    Page<UserCampaignSummaryEntity> findByUserIdOnOrAfter(@Param("userId") UUID userId, @Param("startDate") LocalDate startDate, Pageable pageable);

    @Query("SELECT new ai.salesfox.portal.database.campaign.view.CampaignDateSummaryView(userCampaign.date, COUNT(userCampaign.userId), SUM(userCampaign.recipientCount))" +
            " FROM UserCampaignSummaryEntity userCampaign" +
            " INNER JOIN userCampaign.userEntity user" +
            " INNER JOIN user.membershipEntity membership" +
            " WHERE userCampaign.date >= :startDate" +
            " AND membership.organizationAccountId = :orgAcctId" +
            " GROUP BY userCampaign.date"
    )
    List<CampaignDateSummaryView> summarizeForOrgAccountIdByDateOnOrAfter(@Param("orgAcctId") UUID orgAcctId, @Param("startDate") LocalDate startDate);

}
