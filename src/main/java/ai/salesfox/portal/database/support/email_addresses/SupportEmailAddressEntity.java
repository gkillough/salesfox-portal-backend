package ai.salesfox.portal.database.support.email_addresses;

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
@Table(schema = "portal", name = "support_email_addresses")
public class SupportEmailAddressEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "support_email_id")
    private UUID supportEmailId;

    @Column(name = "category")
    private String supportEmailCategory;

    @Column(name = "email_address")
    private String supportEmailAddress;

}
