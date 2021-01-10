package ai.salesfox.portal.rest.api.support.email_addresses.model;

import ai.salesfox.portal.rest.api.common.page.PagedResponseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MultiSupportEmailAddressModel extends PagedResponseModel {
    private List<SupportEmailAddressResponseModel> supportEmailAddresses;

    public static MultiSupportEmailAddressModel empty() {
        return new MultiSupportEmailAddressModel(List.of(), Page.empty());
    }

    public MultiSupportEmailAddressModel(List<SupportEmailAddressResponseModel> supportEmailAddresses, Page<?> page) {
        super(page);
        this.supportEmailAddresses = supportEmailAddresses;
    }

}
