package ai.salesfox.integration.shipstation.model.custom_store.order;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class InternationalOptionsModel {
    private String contents;
    private List<CustomsItemModel> customsItems;
    private String nonDelivery;

}
