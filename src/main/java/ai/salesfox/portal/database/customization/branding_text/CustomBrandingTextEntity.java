package ai.salesfox.portal.database.customization.branding_text;

import ai.salesfox.portal.database.customization.branding_text.restriction.CustomBrandingTextOrgAccountRestrictionEntity;
import ai.salesfox.portal.database.customization.branding_text.restriction.CustomBrandingTextUserRestrictionEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "custom_branding_texts")
public class CustomBrandingTextEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @PrimaryKeyJoinColumn
    @Column(name = "custom_branding_text_id")
    private UUID customBrandingTextId;

    @Column(name = "custom_branding_text")
    private String customBrandingText;

    @PrimaryKeyJoinColumn
    @Column(name = "uploader_id")
    private UUID uploaderId;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToOne
    @JoinColumn(name = "custom_branding_text_id", referencedColumnName = "custom_branding_text_id", insertable = false, updatable = false)
    private CustomBrandingTextOrgAccountRestrictionEntity customBrandingTextOrgAccountRestrictionEntity;

    @OneToOne
    @JoinColumn(name = "custom_branding_text_id", referencedColumnName = "custom_branding_text_id", insertable = false, updatable = false)
    private CustomBrandingTextUserRestrictionEntity customBrandingTextUserRestrictionEntity;

    @OneToOne
    @JoinColumn(name = "uploader_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private UserEntity uploaderEntity;

    public CustomBrandingTextEntity(UUID customBrandingTextId, String customBrandingText, UUID uploaderId, Boolean isActive) {
        this.customBrandingTextId = customBrandingTextId;
        this.customBrandingText = customBrandingText;
        this.uploaderId = uploaderId;
        this.isActive = isActive;
    }

}
