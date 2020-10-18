package ai.salesfox.portal.database.customization.icon;

import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.customization.icon.restriction.CustomIconOrganizationAccountRestrictionEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "custom_icons")
public class CustomIconEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @PrimaryKeyJoinColumn
    @Column(name = "custom_icon_id")
    private UUID customIconId;

    @Column(name = "label")
    private String label;

    @Column(name = "icon_url")
    private String iconUrl;

    @PrimaryKeyJoinColumn
    @Column(name = "uploader_id")
    private UUID uploaderId;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToOne
    @JoinColumn(name = "custom_icon_id", referencedColumnName = "custom_icon_id", insertable = false, updatable = false)
    private CustomIconOrganizationAccountRestrictionEntity customIconOrganizationAccountRestrictionEntity;

    @OneToOne
    @JoinColumn(name = "uploader_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private UserEntity uploaderEntity;

    public CustomIconEntity(UUID customIconId, String label, String iconUrl, UUID uploaderId, Boolean isActive) {
        this.customIconId = customIconId;
        this.label = label;
        this.iconUrl = iconUrl;
        this.uploaderId = uploaderId;
        this.isActive = isActive;
    }

}
