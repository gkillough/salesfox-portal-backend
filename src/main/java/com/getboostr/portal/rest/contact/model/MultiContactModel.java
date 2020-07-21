package com.getboostr.portal.rest.contact.model;

import com.getboostr.portal.rest.common.page.PagedResponseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MultiContactModel extends PagedResponseModel {
    private List<ContactResponseModel> contacts;

    public static MultiContactModel empty() {
        return new MultiContactModel(List.of(), Page.empty());
    }

    public MultiContactModel(List<ContactResponseModel> contacts, Page<?> page) {
        super(page);
        this.contacts = contacts;
    }

}
