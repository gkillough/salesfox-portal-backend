package ai.salesfox.portal.database.gift.restriction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface GiftOrgAccountRestrictionRepository extends JpaRepository<GiftOrgAccountRestrictionEntity, UUID> {
}
