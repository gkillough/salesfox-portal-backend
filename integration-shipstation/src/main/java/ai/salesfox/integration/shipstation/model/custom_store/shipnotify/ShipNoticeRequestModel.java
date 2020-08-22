package ai.salesfox.integration.shipstation.model.custom_store.shipnotify;

import ai.salesfox.integration.shipstation.model.custom_store.RecipientModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class ShipNoticeRequestModel {
    private String orderId;
    private String orderNumber;
    private String customerCode;
    private String customerNotes;
    private String internalNotes;
    private String notesToCustomer;
    private Boolean notifyCustomer;
    private String labelCreateDate;
    private String shipDate;
    private String carrier;
    private String service;
    private String trackingNumber;
    private BigDecimal shippingCost;
    private RecipientModel recipient;
    private List<ShippedItemModel> items;

}
