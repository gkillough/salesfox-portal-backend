package com.usepipeline.portal.database.catalogue.item;

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
@Table(schema = "portal", name = "catalogue_items")
public class CatalogueItemEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "item_id")
    private UUID itemId;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private String price;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "restricted")
    private Boolean restricted;

    @PrimaryKeyJoinColumn
    @Column(name = "icon_id")
    private UUID icon_id;

}