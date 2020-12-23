package ai.salesfox.portal.database.note.icon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface NoteCustomIconRepository extends JpaRepository<NoteCustomIconEntity, UUID> {
}
