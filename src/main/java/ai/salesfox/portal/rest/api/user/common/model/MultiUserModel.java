package ai.salesfox.portal.rest.api.user.common.model;

import ai.salesfox.portal.rest.api.common.page.PagedResponseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MultiUserModel extends PagedResponseModel {
    private List<UserSummaryModel> users;

    public static MultiUserModel empty() {
        return new MultiUserModel(List.of(), Page.empty());
    }

    public MultiUserModel(List<UserSummaryModel> users, Page<?> page) {
        super(page);
        this.users = users;
    }

}
