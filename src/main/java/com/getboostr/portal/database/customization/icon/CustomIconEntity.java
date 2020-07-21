package com.getboostr.portal.database.customization.icon;

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

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private UUID organizationAccountId;

    @PrimaryKeyJoinColumn
    @Column(name = "uploader_id")
    private UUID uploaderId;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToOne
    @JoinColumn(name = "custom_icon_id", referencedColumnName = "custom_icon_id")
    private CustomIconOwnerEntity customIconOwnerEntity;

    public CustomIconEntity(UUID customIconId, String label, UUID organizationAccountId, UUID uploaderId, Boolean isActive) {
        this.customIconId = customIconId;
        this.label = label;
        this.organizationAccountId = organizationAccountId;
        this.uploaderId = uploaderId;
        this.isActive = isActive;
    }

}
