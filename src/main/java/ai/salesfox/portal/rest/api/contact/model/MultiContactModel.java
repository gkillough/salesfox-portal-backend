package ai.salesfox.portal.rest.api.contact.model;

import ai.salesfox.portal.rest.api.common.page.PagedResponseModel;
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
