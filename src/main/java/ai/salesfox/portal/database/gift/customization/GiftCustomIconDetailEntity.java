package ai.salesfox.portal.database.gift.customization;

import ai.salesfox.portal.database.customization.icon.CustomIconEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "gift_custom_icon_details")
public class GiftCustomIconDetailEntity {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "gift_id")
    private UUID giftId;

    @PrimaryKeyJoinColumn
    @Column(name = "custom_icon_id")
    private UUID customIconId;

    @OneToOne
    @JoinColumn(name = "custom_icon_id", referencedColumnName = "custom_icon_id", updatable = false, insertable = false)
    private CustomIconEntity customIconEntity;

    public GiftCustomIconDetailEntity(UUID giftId, UUID customIconId) {
        this.giftId = giftId;
        this.customIconId = customIconId;
    }

}
