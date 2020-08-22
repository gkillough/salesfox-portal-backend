package ai.salesfox.portal.database.inventory.restriction;

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
@Table(schema = "portal", name = "inventory_user_restrictions")
public class InventoryUserRestrictionEntity implements Serializable {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "inventory_id")
    private UUID inventoryId;

    @PrimaryKeyJoinColumn
    @Column(name = "user_id")
    private UUID userId;

}
