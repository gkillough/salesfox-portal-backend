package ai.salesfox.portal.database.gift.recipient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface GiftRecipientRepository extends JpaRepository<GiftRecipientEntity, GiftRecipientPK> {
}
