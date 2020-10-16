package ai.salesfox.portal.rest.api.license.type.model;

import ai.salesfox.portal.rest.api.common.page.PagedResponseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MultiLicenseTypeModel extends PagedResponseModel {
    private List<LicenseTypeResponseModel> licenseTypes;

    public static MultiLicenseTypeModel empty() {
        return new MultiLicenseTypeModel(List.of(), Page.empty());
    }

    public MultiLicenseTypeModel(List<LicenseTypeResponseModel> licenseTypes, Page<?> page) {
        super(page);
        this.licenseTypes = licenseTypes;
    }

}
