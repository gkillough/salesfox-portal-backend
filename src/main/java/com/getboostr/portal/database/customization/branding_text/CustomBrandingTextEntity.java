package com.getboostr.portal.database.customization.branding_text;

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
    @Column(name = "organization_account_id")
    private UUID organizationAccountId;

    @PrimaryKeyJoinColumn
    @Column(name = "uploader_id")
    private UUID uploaderId;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToOne
    @JoinColumn(name = "custom_branding_text_id", referencedColumnName = "custom_branding_text_id")
    private CustomBrandingTextOwnerEntity customBrandingTextOwnerEntity;

    public CustomBrandingTextEntity(UUID customBrandingTextId, String customBrandingText, UUID organizationAccountId, UUID uploaderId, Boolean isActive) {
        this.customBrandingTextId = customBrandingTextId;
        this.customBrandingText = customBrandingText;
        this.organizationAccountId = organizationAccountId;
        this.uploaderId = uploaderId;
        this.isActive = isActive;
    }

}
