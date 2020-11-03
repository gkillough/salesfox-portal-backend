package ai.salesfox.portal.rest.api.password.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordModel {
    private String token;
    private String email;
    private String newPassword;

}
