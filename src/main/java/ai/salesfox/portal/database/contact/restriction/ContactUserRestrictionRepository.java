package ai.salesfox.portal.database.contact.restriction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface ContactUserRestrictionRepository extends JpaRepository<ContactUserRestrictionEntity, UUID> {
}
