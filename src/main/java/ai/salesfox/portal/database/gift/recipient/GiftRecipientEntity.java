package ai.salesfox.portal.database.gift.recipient;

import ai.salesfox.portal.database.contact.OrganizationAccountContactEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@IdClass(GiftRecipientPK.class)
@Table(schema = "portal", name = "gift_recipients")
public class GiftRecipientEntity implements Serializable {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "gift_id")
    private UUID giftId;

    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "contact_id")
    private UUID contactId;

    @OneToOne
    @JoinColumn(name = "contact_id", referencedColumnName = "contact_id", updatable = false, insertable = false)
    private OrganizationAccountContactEntity organizationAccountContactEntity;

    public GiftRecipientEntity(UUID giftId, UUID contactId) {
        this.giftId = giftId;
        this.contactId = contactId;
    }

}
