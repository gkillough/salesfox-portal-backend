package ai.salesfox.portal.database.note;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface NoteRepository extends JpaRepository<NoteEntity, UUID> {
    Page<NoteEntity> findAllByUpdatedByUserId(UUID updatedByUserId, Pageable pageable);

    @Query("SELECT note" +
            " FROM NoteEntity note" +
            " LEFT JOIN note.noteOrganizationAccountRestrictionEntity orgAcctRestriction" +
            " LEFT JOIN note.noteUserRestrictionEntity userRestriction" +
            " WHERE (" +
            "   (orgAcctRestriction != NULL AND orgAcctRestriction.organizationAccountId = :orgAcctId)" +
            "   OR" +
            "   (userRestriction != NULL AND userRestriction.userId = :userId)" +
            " )"
    )
    Page<NoteEntity> findAccessibleNotes(@Param("orgAcctId") UUID orgAcctId, @Param("userId") UUID userId, Pageable pageable);

}
