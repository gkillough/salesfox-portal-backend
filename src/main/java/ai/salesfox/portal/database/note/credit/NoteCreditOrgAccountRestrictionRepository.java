package ai.salesfox.portal.database.note.credit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface NoteCreditOrgAccountRestrictionRepository extends JpaRepository<NoteCreditOrgAccountRestrictionEntity, UUID> {
}
