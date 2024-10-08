package ai.salesfox.portal.database.catalogue.item;

import ai.salesfox.portal.database.catalogue.external.CatalogueItemExternalDetailsEntity;
import ai.salesfox.portal.database.catalogue.restriction.CatalogueItemOrganizationAccountRestrictionEntity;
import ai.salesfox.portal.database.catalogue.restriction.CatalogueItemUserRestrictionEntity;
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

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "shipping_cost")
    private BigDecimal shippingCost;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToOne
    @JoinColumn(name = "item_id", referencedColumnName = "item_id", insertable = false, updatable = false)
    private CatalogueItemExternalDetailsEntity catalogueItemExternalDetailsEntity;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", referencedColumnName = "item_id", insertable = false, updatable = false)
    private CatalogueItemOrganizationAccountRestrictionEntity catalogueItemOrganizationAccountRestrictionEntity;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", referencedColumnName = "item_id", insertable = false, updatable = false)
    private CatalogueItemUserRestrictionEntity catalogueItemUserRestrictionEntity;

    public CatalogueItemEntity(UUID itemId, String name, String description, BigDecimal price, BigDecimal shippingCost, String iconUrl, Boolean isActive) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.shippingCost = shippingCost;
        this.iconUrl = iconUrl;
        this.isActive = isActive;
    }

    public boolean hasRestriction() {
        return null != catalogueItemOrganizationAccountRestrictionEntity || null != catalogueItemUserRestrictionEntity;
    }

}
