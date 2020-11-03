package ai.salesfox.portal.rest.api.password.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetValidationResponseModel {
    private Boolean isValid;
    private String message;

}
