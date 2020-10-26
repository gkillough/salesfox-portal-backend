package ai.salesfox.portal.integration.scribeless.workflow.model;

import ai.salesfox.integration.scribeless.service.campaign.model.CampaignCreationRequestModel;
import ai.salesfox.portal.common.model.PagedResourceHolder;
import ai.salesfox.portal.database.gift.recipient.GiftRecipientEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.function.Function;

@EqualsAndHashCode(callSuper = true)
public class CampaignCreationRequestHolder extends PagedResourceHolder<GiftRecipientEntity> {
    @Getter
    private final CampaignCreationRequestModel campaignCreationRequestModel;

    public CampaignCreationRequestHolder(CampaignCreationRequestModel campaignCreationRequestModel, Page<GiftRecipientEntity> firstPage, Function<Pageable, Page<GiftRecipientEntity>> retrieveNextPageFunction) {
        super(firstPage, retrieveNextPageFunction);
        this.campaignCreationRequestModel = campaignCreationRequestModel;
    }

}
