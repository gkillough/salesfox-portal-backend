package com.usepipeline.portal.database.catalogue.restriction;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "catalogue_item_restrictions")
public class CatalogueItemRestrictionEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "restriction_id")
    private UUID restrictionId;

    @PrimaryKeyJoinColumn
    @Column(name = "item_id")
    private UUID itemId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private UUID organizationAccountId;

    @PrimaryKeyJoinColumn
    @Column(name = "user_id")
    private UUID userId;

    public CatalogueItemRestrictionEntity(UUID restrictionId, UUID itemId, UUID organizationAccountId, UUID userId) {
        this.restrictionId = restrictionId;
        this.itemId = itemId;
        this.organizationAccountId = organizationAccountId;
        this.userId = userId;
    }

}