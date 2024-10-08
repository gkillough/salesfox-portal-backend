package ai.salesfox.portal.database.organization;

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
@Table(schema = "portal", name = "organizations")
public class OrganizationEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "is_active")
    private Boolean isActive;

}
