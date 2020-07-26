package com.getboostr.portal.database.gift;

import com.getboostr.portal.database.gift.customization.GiftCustomizationDetailEntity;
import com.getboostr.portal.database.gift.item.GiftItemDetailEntity;
import com.getboostr.portal.database.gift.note.GiftNoteDetailEntity;
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
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "gift_id")
    private UUID giftId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private UUID organizationAccountId;

    @PrimaryKeyJoinColumn
    @Column(name = "requesting_user_id")
    private UUID requestingUserId;

    @PrimaryKeyJoinColumn
    @Column(name = "contact_id")
    private UUID contactId;

    @OneToOne
    @JoinColumn(name = "gift_id", referencedColumnName = "gift_id", updatable = false, insertable = false)
    private GiftNoteDetailEntity giftNoteDetailEntity;

    @OneToOne
    @JoinColumn(name = "gift_id", referencedColumnName = "gift_id", updatable = false, insertable = false)
    private GiftItemDetailEntity giftItemDetailEntity;

    @OneToOne
    @JoinColumn(name = "gift_id", referencedColumnName = "gift_id", updatable = false, insertable = false)
    private GiftCustomizationDetailEntity giftCustomizationDetailEntity;

    @OneToOne
    @JoinColumn(name = "gift_id", referencedColumnName = "gift_id", updatable = false, insertable = false)
    private GiftTrackingEntity giftTrackingEntity;

    @OneToOne
    @JoinColumn(name = "gift_id", referencedColumnName = "gift_id", updatable = false, insertable = false)
    private GiftTrackingDetailEntity giftTrackingDetailEntity;

    public GiftEntity(UUID giftId, UUID organizationAccountId, UUID requestingUserId, UUID contactId) {
        this.giftId = giftId;
        this.organizationAccountId = organizationAccountId;
        this.requestingUserId = requestingUserId;
        this.contactId = contactId;
    }

}
