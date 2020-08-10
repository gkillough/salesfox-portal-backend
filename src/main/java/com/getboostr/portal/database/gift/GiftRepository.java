package com.getboostr.portal.database.gift;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

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

}
