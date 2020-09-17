package ai.salesfox.portal.database.gift.recipient;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface GiftRecipientRepository extends JpaRepository<GiftRecipientEntity, GiftRecipientPK> {
    Page<GiftRecipientEntity> findByGiftId(UUID giftId, Pageable pageable);

    void deleteByGiftId(UUID giftId);

}
