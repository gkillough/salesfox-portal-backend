package com.usepipeline.portal.web.contact.model;

import com.usepipeline.portal.web.common.page.PagedResponseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MultiContactModel extends PagedResponseModel {
    private List<ContactModel> contacts;

    public static MultiContactModel empty() {
        return new MultiContactModel(Collections.emptyList(), Page.empty());
    }

    public MultiContactModel(List<ContactModel> contacts, Page<?> page) {
        super(page);
        this.contacts = contacts;
    }

}
