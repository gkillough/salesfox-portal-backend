package com.usepipeline.portal.database.catalogue.restriction;

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
    private UUID organization_account_id;

    @PrimaryKeyJoinColumn
    @Column(name = "user_id")
    private UUID user_id;

}