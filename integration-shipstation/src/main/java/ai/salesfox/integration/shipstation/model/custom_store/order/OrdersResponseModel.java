package ai.salesfox.integration.shipstation.model.custom_store.order;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class OrdersResponseModel {
    private List<OrderModel> orders;
    private Integer total;
    private Integer page;
    private Integer pages;

}
