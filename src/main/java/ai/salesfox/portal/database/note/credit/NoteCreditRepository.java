package ai.salesfox.portal.database.note.credit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface NoteCreditRepository extends JpaRepository<NoteCreditEntity, UUID> {
    @Query("SELECT noteCredit" +
            " FROM NoteCreditEntity noteCredit" +
            " LEFT JOIN noteCredit.noteCreditOrgAccountRestrictionEntity orgAcctRestriction" +
            " LEFT JOIN noteCredit.noteCreditUserRestrictionEntity userRestriction" +
            " WHERE (" +
            "   (orgAcctRestriction != NULL AND orgAcctRestriction.organizationAccountId = :orgAcctId)" +
            "   OR" +
            "   (userRestriction != NULL AND userRestriction.userId = :userId)" +
            " )"
    )
    Page<NoteCreditEntity> findAccessibleNoteCredits(@Param("orgAcctId") UUID orgAcctId, @Param("userId") UUID userId, Pageable pageable);

}
