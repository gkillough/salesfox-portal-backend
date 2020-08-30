package ai.salesfox.integration.shipstation.model.custom_store.order;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class OrderModel {
    private String orderId;
    private String orderNumber;
    private String orderDate;
    private String orderStatus;
    private String lastModified;
    private String shippingMethod;
    private String paymentMethod;
    private BigDecimal orderTotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private String customerNotes;
    private String internalNotes;
    private Boolean gift;
    private String giftMessage;
    private String customField1;
    private String customField2;
    private String customField3;
    private String requestedWarehouse;
    private String source;
    private CustomerModel customer;
    private List<OrderItemModel> items;

}
