package ai.salesfox.portal.rest.api.license.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiLicenseModel {
    private List<LicenseModel> licenses;

}
