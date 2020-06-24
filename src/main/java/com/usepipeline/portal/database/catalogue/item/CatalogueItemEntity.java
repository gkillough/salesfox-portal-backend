package com.usepipeline.portal.database.catalogue.item;

import com.usepipeline.portal.database.catalogue.restriction.CatalogueItemRestrictionEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "catalogue_items")
public class CatalogueItemEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @PrimaryKeyJoinColumn
    @Column(name = "item_id")
    private UUID itemId;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "restricted")
    private Boolean restricted;

    @PrimaryKeyJoinColumn
    @Column(name = "icon_id")
    private UUID iconId;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", referencedColumnName = "item_id")
    private CatalogueItemRestrictionEntity catalogueItemRestrictionEntity;

    public CatalogueItemEntity(UUID itemId, String name, BigDecimal price, Long quantity, Boolean restricted, UUID iconId, Boolean isActive) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.restricted = restricted;
        this.iconId = iconId;
        this.isActive = isActive;
    }

}