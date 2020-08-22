package ai.salesfox.portal.database.gift.restriction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "gift_org_account_restrictions")
public class GiftOrgAccountRestrictionEntity implements Serializable {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "gift_id")
    private UUID giftId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private UUID orgAccountId;

}
