package com.getboostr.portal.database.customization.branding_text.restriction;

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
@Table(schema = "portal", name = "custom_branding_text_user_restrictions")
public class CustomBrandingTextUserRestrictionEntity implements Serializable {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "custom_branding_text_id")
    private UUID customBrandingTextId;

    @PrimaryKeyJoinColumn
    @Column(name = "user_id")
    private UUID userId;

}
