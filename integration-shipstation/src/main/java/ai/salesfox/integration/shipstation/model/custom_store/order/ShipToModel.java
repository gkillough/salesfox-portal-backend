package ai.salesfox.integration.shipstation.model.custom_store.order;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ShipToModel {
    private String name;
    private String company;
    private String address1;
    private String address2;
    private String address3;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String phone;

}
