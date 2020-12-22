package ai.salesfox.portal.rest.api.support.email_addresses.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupportEmailAddressRequestModel {
    private String category;
    private String emailAddress;

}
