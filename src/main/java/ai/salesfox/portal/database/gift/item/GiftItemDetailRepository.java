package ai.salesfox.portal.database.gift.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface GiftItemDetailRepository extends JpaRepository<GiftItemDetailEntity, UUID> {

}
