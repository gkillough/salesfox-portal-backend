package ai.salesfox.portal.rest.api.registration.plan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationPlanModel {
    private String name;
    private String roleLevel;
    private String description;

}
