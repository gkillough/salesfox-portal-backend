package ai.salesfox.integration.shipstation.model.custom_store.order;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BillToModel {
    private String name;
    private String company;
    private String phone;
    private String email;

}
