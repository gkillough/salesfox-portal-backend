package ai.salesfox.portal.rest.api.support.email_addresses.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupportEmailAddressResponseModel {
    private UUID supportEmailAddressId;
    private String category;
    private String emailAddress;

}
