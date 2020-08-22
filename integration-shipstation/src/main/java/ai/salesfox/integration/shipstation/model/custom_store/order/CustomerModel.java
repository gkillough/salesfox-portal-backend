package ai.salesfox.integration.shipstation.model.custom_store.order;

import ai.salesfox.integration.shipstation.model.custom_store.RecipientModel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CustomerModel {
    private String customerCode;
    private BillToModel billTo;
    private RecipientModel shipTo;

}
