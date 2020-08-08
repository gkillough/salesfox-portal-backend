package com.getboostr.portal.database.gift;

import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.contact.OrganizationAccountContactEntity;
import com.getboostr.portal.database.gift.customization.GiftCustomIconDetailEntity;
import com.getboostr.portal.database.gift.customization.GiftCustomTextDetailEntity;
import com.getboostr.portal.database.gift.item.GiftItemDetailEntity;
import com.getboostr.portal.database.gift.note.GiftNoteDetailEntity;
import com.getboostr.portal.database.gift.restriction.GiftOrgAccountRestrictionEntity;
import com.getboostr.portal.database.gift.restriction.GiftUserRestrictionEntity;
import com.getboostr.portal.database.gift.tracking.GiftTrackingDetailEntity;
import com.getboostr.portal.database.gift.tracking.GiftTrackingEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
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

    @PrimaryKeyJoinColumn
    @Column(name = "contact_id")
    private UUID contactId;

    @OneToOne
    @JoinColumn(name = "requesting_user_id", referencedColumnName = "user_id", updatable = false, insertable = false)
    private UserEntity requestingUserEntity;

    @OneToOne
    @JoinColumn(name = "contact_id", referencedColumnName = "contact_id", updatable = false, insertable = false)
    private OrganizationAccountContactEntity contactEntity;

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

    public GiftEntity(UUID giftId, UUID requestingUserId, UUID contactId) {
        this.giftId = giftId;
        this.requestingUserId = requestingUserId;
        this.contactId = contactId;
    }

}
