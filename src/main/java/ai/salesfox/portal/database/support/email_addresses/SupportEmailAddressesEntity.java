package ai.salesfox.portal.database.support.email_addresses;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "support_email_addresses")
public class SupportEmailAddressesEntity implements Serializable {
    @Id
    @Column(name = "id")
    private UUID supportEmailId;

    @Column(name = "category")
    private String supportEmailCategory;

    @Column(name = "email_address")
    private String supportEmailAddress;

    public SupportEmailAddressesEntity(UUID supportEmailId, String supportEmailCategory, String supportEmailAddress) {
        this.supportEmailId = supportEmailId;
        this.supportEmailCategory = supportEmailCategory;
        this.supportEmailAddress = supportEmailAddress;
    }

}
