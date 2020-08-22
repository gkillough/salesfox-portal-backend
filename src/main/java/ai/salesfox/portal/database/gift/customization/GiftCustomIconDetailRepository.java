package ai.salesfox.portal.database.gift.customization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public interface GiftCustomIconDetailRepository extends JpaRepository<GiftCustomIconDetailEntity, UUID> {
    List<GiftCustomIconDetailEntity> findByCustomIconId(UUID customIconId);

}
