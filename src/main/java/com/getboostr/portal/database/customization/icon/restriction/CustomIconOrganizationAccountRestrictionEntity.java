package com.getboostr.portal.database.customization.icon.restriction;

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
@Table(schema = "portal", name = "custom_icon_organization_account_restrictions")
public class CustomIconOrganizationAccountRestrictionEntity implements Serializable {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "custom_icon_id")
    private UUID customIconId;

    @Column(name = "organization_account_id")
    private UUID organizationAccountId;

}
