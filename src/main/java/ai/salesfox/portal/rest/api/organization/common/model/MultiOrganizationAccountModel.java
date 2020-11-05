package ai.salesfox.portal.rest.api.organization.common.model;

import ai.salesfox.portal.rest.api.common.page.PagedResponseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MultiOrganizationAccountModel extends PagedResponseModel {
    private List<OrganizationAccountSummaryModel> organizationAccounts;

    public static MultiOrganizationAccountModel empty() {
        return new MultiOrganizationAccountModel(List.of(), Page.empty());
    }

    public MultiOrganizationAccountModel(List<OrganizationAccountSummaryModel> organizationAccounts, Page<?> page) {
        super(page);
        this.organizationAccounts = organizationAccounts;
    }

}
