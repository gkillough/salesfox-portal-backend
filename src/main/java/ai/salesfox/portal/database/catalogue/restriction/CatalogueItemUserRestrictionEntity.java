package ai.salesfox.portal.database.catalogue.restriction;

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
@Table(schema = "portal", name = "catalogue_item_user_restrictions")
public class CatalogueItemUserRestrictionEntity implements Serializable {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "item_id")
    private UUID itemId;

    @PrimaryKeyJoinColumn
    @Column(name = "user_id")
    private UUID userId;

}
