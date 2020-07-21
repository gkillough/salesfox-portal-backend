package com.getboostr.portal.database.gift;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface GiftRepository extends JpaRepository<GiftEntity, UUID> {
    @Query("SELECT gift" +
            " FROM GiftEntity gift" +
            " WHERE gift.organizationAccountId = :orgAcctId"
    )
    Page<GiftEntity> findAllByOrganizationAccountId(@Param("orgAcctId") UUID orgAcctId, Pageable pageable);

    @Query("SELECT gift" +
            " FROM GiftEntity gift" +
            " WHERE gift.requestingUserId = :requestingUserId"
    )
    Page<GiftEntity> findAllByRequestingUserId(@Param("requestingUserId") UUID requestingUserId, Pageable pageable);

}
