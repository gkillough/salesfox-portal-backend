package ai.salesfox.integration.scribeless.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScribelessAddressModel {
    private String title;
    @SerializedName("first name")
    private String firstName;
    @SerializedName("last name")
    private String lastName;
    private String department;
    @SerializedName("address line 1")
    private String addressLine1;
    @SerializedName("address line 2")
    private String addressLine2;
    @SerializedName("address line 3")
    private String addressLine3;
    private String city;
    private String country;
    @SerializedName("state/region")
    private String stateRegion;
    @SerializedName("zip/postal code")
    private String zipPostalCode;

}
