package ai.salesfox.integration.scribeless.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScribelessAddressModel {
    /*
    {
      "address line 1": "Flat 1",
      "address line 2": "123 Broom Road",
      "address line 3": "Bathwick hill",
      "city": "London",
      "country": "United Kingdom",
      "department": "HR",
      "first name": "Tim",
      "last name": "Johnson",
      "state/region": "London",
      "title": "Mr",
      "zip/postal code": "TW11 9PG"
    }
     */
    private String title;
    private String firstName;
    private String lastName;
    private String department;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String city;
    private String country;
    private String stateRegion;
    private String zipPostalCode;

}
