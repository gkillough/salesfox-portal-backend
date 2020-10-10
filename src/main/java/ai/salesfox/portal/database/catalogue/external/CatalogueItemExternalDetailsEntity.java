package ai.salesfox.portal.database.catalogue.external;

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
@Table(schema = "portal", name = "catalogue_item_external_details")
public class CatalogueItemExternalDetailsEntity implements Serializable {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "item_id")
    private UUID itemId;

    @Column(name = "distributor")
    private String distributor;

    @Column(name = "external_id")
    private String externalId;

}
