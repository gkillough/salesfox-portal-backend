package ai.salesfox.portal.integration.scribeless.workflow.model;

import ai.salesfox.integration.scribeless.service.campaign.model.CampaignCreationRequestModel;
import ai.salesfox.portal.database.gift.recipient.GiftRecipientEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.function.Function;

@AllArgsConstructor
public class CampaignCreationRequestHolder {
    @Getter
    private final CampaignCreationRequestModel campaignCreationRequestModel;
    @Getter
    private final Page<GiftRecipientEntity> firstPageOfRecipients;

    private final Function<Pageable, Page<GiftRecipientEntity>> retrieveNextPageFunction;

    public Page<GiftRecipientEntity> retrieveNextPageOfRecipients(Page<GiftRecipientEntity> currentPage) {
        if (currentPage.hasNext()) {
            return retrieveNextPageFunction.apply(currentPage.nextPageable());
        }
        return Page.empty();
    }

}
