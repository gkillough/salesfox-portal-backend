package ai.salesfox.portal.database.gift.tracking;

import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.common.enumeration.GiftTrackingStatus;
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
    @JoinColumn(name = "updated_by_user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private UserEntity updatedByUserEntity;

    @OneToOne
    @JoinColumn(name = "gift_id", referencedColumnName = "gift_id", insertable = false, updatable = false)
    private GiftTrackingDetailEntity giftTrackingDetailEntity;

    public GiftTrackingEntity(UUID giftId, String status, UUID updatedByUserId, OffsetDateTime dateCreated, OffsetDateTime dateUpdated) {
        this.giftId = giftId;
        this.status = status;
        this.updatedByUserId = updatedByUserId;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
    }

    public boolean isSubmittable() {
        return isDraft() || isScheduled();
    }

    public boolean isCancellable() {
        return isSubmitted() || isPackaged();
    }

    public boolean isDraft() {
        return hasStatus(GiftTrackingStatus.DRAFT);
    }

    public boolean isScheduled() {
        return hasStatus(GiftTrackingStatus.SCHEDULED);
    }

    public boolean isSubmitted() {
        return hasStatus(GiftTrackingStatus.SUBMITTED);
    }

    public boolean isPackaged() {
        return hasStatus(GiftTrackingStatus.PACKAGED);
    }

    public boolean isCancelled() {
        return hasStatus(GiftTrackingStatus.CANCELLED);
    }

    public boolean hasStatus(GiftTrackingStatus status) {
        return status.name().equals(this.status);
    }

}
