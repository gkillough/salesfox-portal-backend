package ai.salesfox.portal.database.gift.tracking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "gift_tracking_details")
public class GiftTrackingDetailEntity {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "gift_id")
    private UUID giftId;

    @Column(name = "distributor")
    private String distributor;

    @Column(name = "tracking_number")
    private String trackingNumber;

}
