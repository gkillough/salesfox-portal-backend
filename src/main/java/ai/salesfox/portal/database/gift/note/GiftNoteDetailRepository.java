package ai.salesfox.portal.database.gift.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public interface GiftNoteDetailRepository extends JpaRepository<GiftNoteDetailEntity, UUID> {
    List<GiftNoteDetailEntity> findByNoteId(UUID noteId);

}
