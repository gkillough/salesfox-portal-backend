package ai.salesfox.portal.database.gift;

import ai.salesfox.portal.common.enumeration.GiftTrackingStatus;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactEntity;
import ai.salesfox.portal.database.gift.customization.GiftCustomIconDetailEntity;
import ai.salesfox.portal.database.gift.customization.GiftCustomTextDetailEntity;
import ai.salesfox.portal.database.gift.item.GiftItemDetailEntity;
import ai.salesfox.portal.database.gift.note.GiftNoteDetailEntity;
import ai.salesfox.portal.database.gift.restriction.GiftOrgAccountRestrictionEntity;
import ai.salesfox.portal.database.gift.restriction.GiftUserRestrictionEntity;
import ai.salesfox.portal.database.gift.scheduling.GiftScheduleEntity;
import ai.salesfox.portal.database.gift.tracking.GiftTrackingDetailEntity;
import ai.salesfox.portal.database.gift.tracking.GiftTrackingEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "gifts")
public class GiftEntity implements Serializable {
    @Id
    @PrimaryKeyJoinColumn
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "gift_id")
    private UUID giftId;

    @PrimaryKeyJoinColumn
    @Column(name = "requesting_user_id")
    private UUID requestingUserId;

    @OneToOne
    @JoinColumn(name = "requesting_user_id", referencedColumnName = "user_id", updatable = false, insertable = false)
    private UserEntity requestingUserEntity;

    // FIXME remove for the sake of paging
    @ManyToMany
    @JoinTable(
            schema = "portal",
            name = "gift_recipients",
            joinColumns = {
                    @JoinColumn(name = "gift_id", referencedColumnName = "gift_id", updatable = false, insertable = false)
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "contact_id", referencedColumnName = "contact_id", updatable = false, insertable = false)
            }
    )
    private List<OrganizationAccountContactEntity> giftRecipients;

    @OneToOne
    @JoinColumn(name = "gift_id", referencedColumnName = "gift_id", updatable = false, insertable = false)
    private GiftOrgAccountRestrictionEntity giftOrgAccountRestrictionEntity;

    @OneToOne
    @JoinColumn(name = "gift_id", referencedColumnName = "gift_id", updatable = false, insertable = false)
    private GiftUserRestrictionEntity giftUserRestrictionEntity;

    @OneToOne
    @JoinColumn(name = "gift_id", referencedColumnName = "gift_id", updatable = false, insertable = false)
    private GiftNoteDetailEntity giftNoteDetailEntity;

    @OneToOne
    @JoinColumn(name = "gift_id", referencedColumnName = "gift_id", updatable = false, insertable = false)
    private GiftItemDetailEntity giftItemDetailEntity;

    @OneToOne
    @JoinColumn(name = "gift_id", referencedColumnName = "gift_id", updatable = false, insertable = false)
    private GiftCustomIconDetailEntity giftCustomIconDetailEntity;

    @OneToOne
    @JoinColumn(name = "gift_id", referencedColumnName = "gift_id", updatable = false, insertable = false)
    private GiftCustomTextDetailEntity giftCustomTextDetailEntity;

    @OneToOne
    @JoinColumn(name = "gift_id", referencedColumnName = "gift_id", updatable = false, insertable = false)
    private GiftTrackingEntity giftTrackingEntity;

    @OneToOne
    @JoinColumn(name = "gift_id", referencedColumnName = "gift_id", updatable = false, insertable = false)
    private GiftTrackingDetailEntity giftTrackingDetailEntity;

    @OneToOne
    @JoinColumn(name = "gift_id", referencedColumnName = "gift_id", updatable = false, insertable = false)
    private GiftScheduleEntity giftScheduleEntity;

    public GiftEntity(UUID giftId, UUID requestingUserId) {
        this.giftId = giftId;
        this.requestingUserId = requestingUserId;
    }

    public boolean isSubmittable() {
        return giftTrackingEntity.isSubmittable();
    }

    public boolean isCancellable() {
        return giftTrackingEntity.isCancellable();
    }

    public boolean isDraft() {
        return giftTrackingEntity.isDraft();
    }

    public boolean isScheduled() {
        return giftTrackingEntity.isScheduled();
    }

    public boolean isSubmitted() {
        return giftTrackingEntity.isSubmitted();
    }

    public boolean isPackaged() {
        return giftTrackingEntity.isPackaged();
    }

    public boolean isCancelled() {
        return giftTrackingEntity.isCancelled();
    }

    public boolean hasStatus(GiftTrackingStatus status) {
        return giftTrackingEntity.hasStatus(status);
    }

}
