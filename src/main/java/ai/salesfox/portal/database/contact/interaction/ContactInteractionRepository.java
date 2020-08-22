package ai.salesfox.portal.database.contact.interaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface ContactInteractionRepository extends JpaRepository<ContactInteractionEntity, UUID> {
    Page<ContactInteractionEntity> findAllByContactId(UUID contactId, Pageable pageable);

}
