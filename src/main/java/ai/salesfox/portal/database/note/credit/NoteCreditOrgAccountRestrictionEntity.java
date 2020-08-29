package ai.salesfox.portal.database.note.credit;

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
@Table(schema = "portal", name = "note_credit_organization_account_restrictions")
public class NoteCreditOrgAccountRestrictionEntity implements Serializable {
    @Id
    @Column(name = "note_credit_id")
    private UUID noteCreditId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private UUID organizationAccountId;

}
