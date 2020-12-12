package ai.salesfox.portal.database.note.credit;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "note_credit_price")
public class NoteCreditPriceEntity implements Serializable {
    @Id
    @Column(name = "price_id")
    private UUID noteCreditPriceId;

    @Column(name = "price")
    private BigDecimal noteCreditPrice;

    public NoteCreditPriceEntity(UUID noteCreditPriceId, BigDecimal noteCreditPrice) {
        this.noteCreditPriceId = noteCreditPriceId;
        this.noteCreditPrice = noteCreditPrice;
    }

}
