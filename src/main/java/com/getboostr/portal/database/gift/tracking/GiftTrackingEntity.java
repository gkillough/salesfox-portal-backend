package com.getboostr.portal.database.gift.tracking;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "gift_tracking")
public class GiftTrackingEntity implements Serializable {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "gift_id")
    private UUID giftId;

    @Column(name = "status")
    private String status;

    @PrimaryKeyJoinColumn
    @Column(name = "updated_by_user_id")
    private UUID updatedByUserId;

    @Column(name = "date_created")
    private OffsetDateTime dateCreated;

    @Column(name = "date_updated")
    private OffsetDateTime dateUpdated;

    @OneToOne
    @JoinColumn(name = "gift_id", referencedColumnName = "gift_id")
    private GiftTrackingDetailEntity giftTrackingDetailEntity;

    public GiftTrackingEntity(UUID giftId, String status, UUID updatedByUserId, OffsetDateTime dateCreated, OffsetDateTime dateUpdated) {
        this.giftId = giftId;
        this.status = status;
        this.updatedByUserId = updatedByUserId;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
    }

}
