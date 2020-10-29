package ai.salesfox.portal.integration.scribeless.workflow.model;

import ai.salesfox.integration.scribeless.service.campaign.model.CampaignCreationRequestModel;
import ai.salesfox.portal.common.model.PagedResourceHolder;
import ai.salesfox.portal.database.contact.OrganizationAccountContactEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
public class CampaignCreationRequestHolder extends PagedResourceHolder<OrganizationAccountContactEntity> {
    @Getter
    private final CampaignCreationRequestModel campaignCreationRequestModel;

    public CampaignCreationRequestHolder(CampaignCreationRequestModel campaignCreationRequestModel, PagedResourceHolder<OrganizationAccountContactEntity> recipientHolder) {
        super(recipientHolder);
        this.campaignCreationRequestModel = campaignCreationRequestModel;
    }

}
