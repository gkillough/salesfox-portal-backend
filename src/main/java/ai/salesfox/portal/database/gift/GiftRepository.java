package ai.salesfox.portal.database.gift;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public interface GiftRepository extends JpaRepository<GiftEntity, UUID> {
    @Query("SELECT gift" +
            " FROM GiftEntity gift" +
            " LEFT JOIN gift.giftOrgAccountRestrictionEntity orgAcctRestriction" +
            " LEFT JOIN gift.giftUserRestrictionEntity userRestriction" +
            " LEFT JOIN gift.giftTrackingEntity tracking" +
            " WHERE (" +
            "   (orgAcctRestriction != NULL AND orgAcctRestriction.orgAccountId = :orgAcctId)" +
            "   OR" +
            "   (userRestriction != NULL AND userRestriction.userId = :userId)" +
            " )" +
            " AND (" +
            "   :status = NULL OR tracking.status = :status" +
            " )" +
            " ORDER BY tracking.dateCreated"
    )
    Page<GiftEntity> findAccessibleGiftsByStatus(@Param("orgAcctId") UUID orgAcctId, @Param("userId") UUID userId, @Nullable @Param("status") String status, Pageable pageable);

    @Query("SELECT gift" +
            " FROM GiftEntity gift" +
            " LEFT JOIN gift.giftTrackingEntity tracking" +
            " INNER JOIN gift.giftScheduleEntity schedule" +
            " WHERE (" +
            "   tracking.status = :scheduledStatusName" +
            "   AND :sendDate = schedule.sendDate" +
            " )"
    )
    Slice<GiftEntity> findScheduledGiftsBySendDate(@Param("scheduledStatusName") String scheduledStatusName, @Param("sendDate") LocalDate sendDate, Pageable pageable);

}
