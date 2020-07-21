package com.getboostr.portal.rest.api.organization.users.model;

import com.getboostr.portal.rest.api.common.page.PagedResponseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrganizationMultiUsersModel extends PagedResponseModel {
    private List<OrganizationUserAdminViewModel> users;

    public OrganizationMultiUsersModel(List<OrganizationUserAdminViewModel> users, Page<?> page) {
        super(page);
        this.users = users;
    }

}
