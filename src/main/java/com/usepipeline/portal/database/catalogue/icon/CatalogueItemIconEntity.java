package com.usepipeline.portal.database.catalogue.icon;

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
@Table(schema = "portal", name = "catalogue_item_icons")
public class CatalogueItemIconEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "icon_id")
    private UUID iconId;

    @Column(name = "location_uri")
    private String locationUri;

    @Column(name = "external")
    private Boolean external;

}
