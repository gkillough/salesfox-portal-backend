package ai.salesfox.portal.rest.api.csrf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CsrfTokenHolderResponseModel {
    private String headerName;
    private String headerValue;

}
