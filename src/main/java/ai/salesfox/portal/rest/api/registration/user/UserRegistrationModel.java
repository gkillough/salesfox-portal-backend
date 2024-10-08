package ai.salesfox.portal.rest.api.registration.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationModel {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String roleLevel;

}
