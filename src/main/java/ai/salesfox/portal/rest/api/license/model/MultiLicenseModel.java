package ai.salesfox.portal.rest.api.license.model;

import ai.salesfox.portal.rest.api.common.page.PagedResponseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MultiLicenseModel extends PagedResponseModel {
    private List<LicenseModel> licenses;

    public static MultiLicenseModel empty() {
        return new MultiLicenseModel(List.of(), Page.empty());
    }

    public MultiLicenseModel(List<LicenseModel> licenses, Page<?> page) {
        super(page);
        this.licenses = licenses;
    }

}
