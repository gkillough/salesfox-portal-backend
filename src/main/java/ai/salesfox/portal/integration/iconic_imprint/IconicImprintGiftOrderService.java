package ai.salesfox.portal.integration.iconic_imprint;

import ai.salesfox.portal.common.enumeration.DistributorName;
import ai.salesfox.portal.common.exception.PortalException;
import ai.salesfox.portal.common.model.PagedResourceHolder;
import ai.salesfox.portal.common.service.email.EmailMessagingService;
import ai.salesfox.portal.common.service.gift.GiftDetailsService;
import ai.salesfox.portal.database.contact.OrganizationAccountContactEntity;
import ai.salesfox.portal.integration.EmailSubmissionGiftPartner;
import ai.salesfox.portal.integration.noms.workflow.NomsRecipientCSVGenerator;
import ai.salesfox.portal.integration.scribeless.workflow.ScribelessCampaignRequestModelCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.UUID;

@Component
public class IconicImprintGiftOrderService extends EmailSubmissionGiftPartner {
    private final IconicImprintConfiguration iconicImprintConfiguration;

    // TODO Temporarily use NOMs recipient CSV format until we
    //  determine a more preferable format for Iconic Imprint
    private final NomsRecipientCSVGenerator nomsRecipientCSVGenerator;

    @Autowired
    public IconicImprintGiftOrderService(
            GiftDetailsService giftDetailsService,
            ScribelessCampaignRequestModelCreator scribelessCampaignRequestModelCreator,
            EmailMessagingService emailMessagingService,
            IconicImprintConfiguration iconicImprintConfiguration,
            NomsRecipientCSVGenerator nomsRecipientCSVGenerator
    ) {
        super(giftDetailsService, scribelessCampaignRequestModelCreator, emailMessagingService);
        this.iconicImprintConfiguration = iconicImprintConfiguration;
        this.nomsRecipientCSVGenerator = nomsRecipientCSVGenerator;
    }

    @Override
    public DistributorName distributorName() {
        return DistributorName.ICONIC_IMPRINT;
    }

    @Override
    protected List<String> retrieveOrderEmailAddresses() {
        return List.of(iconicImprintConfiguration.getIconicImprintOrderEmailAddress());
    }

    @Override
    protected File generateRecipientCSVFileToAttach(UUID giftId, PagedResourceHolder<OrganizationAccountContactEntity> contactPageHolder) throws PortalException {
        return nomsRecipientCSVGenerator.createRecipientCSVFile(giftId, contactPageHolder);
    }

}
