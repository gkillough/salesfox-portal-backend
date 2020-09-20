package ai.salesfox.portal.rest.api.gift.recipient;

import ai.salesfox.portal.rest.api.common.page.PagedResponseModel;
import ai.salesfox.portal.rest.api.contact.model.ContactSummaryModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MultiGiftRecipientResponseModel extends PagedResponseModel {
    private List<ContactSummaryModel> recipients;

    public static MultiGiftRecipientResponseModel empty() {
        return new MultiGiftRecipientResponseModel(List.of(), Page.empty());
    }

    public MultiGiftRecipientResponseModel(List<ContactSummaryModel> recipients, Page<?> page) {
        super(page);
        this.recipients = recipients;
    }

}
