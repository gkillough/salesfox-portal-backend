package ai.salesfox.portal.database.customization.branding_text.restriction;

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
@Table(schema = "portal", name = "custom_branding_text_org_account_restrictions")
public class CustomBrandingTextOrgAccountRestrictionEntity implements Serializable {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "custom_branding_text_id")
    private UUID customBrandingTextId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private UUID orgAccountId;

}
