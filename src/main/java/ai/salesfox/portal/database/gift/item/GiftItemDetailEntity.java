package ai.salesfox.portal.database.gift.item;

import ai.salesfox.portal.database.catalogue.item.CatalogueItemEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "gift_item_details")
public class GiftItemDetailEntity {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "gift_id")
    private UUID giftId;

    @PrimaryKeyJoinColumn
    @Column(name = "item_id")
    private UUID itemId;

    public GiftItemDetailEntity(UUID giftId, UUID itemId) {
        this.giftId = giftId;
        this.itemId = itemId;
    }

    @OneToOne
    @JoinColumn(name = "item_id", referencedColumnName = "item_id", updatable = false, insertable = false)
    private CatalogueItemEntity catalogueItemEntity;

}
