package ai.salesfox.portal.database.note.credit;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "note_credit_price")
public class NoteCreditPriceEntity implements Serializable {
    @Id
    @Column(name = "price_id")
    private Integer noteCreditPriceId;
    
    @Column(name = "price")
    private Double noteCreditPrice;

    public NoteCreditPriceEntity(Integer noteCreditPriceId, Double noteCreditPrice) {
        this.noteCreditPriceId = noteCreditPriceId;
        this.noteCreditPrice = noteCreditPrice;
    }

}
