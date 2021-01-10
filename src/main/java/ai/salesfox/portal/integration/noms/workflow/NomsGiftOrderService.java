package ai.salesfox.portal.integration.noms.workflow;

import ai.salesfox.portal.common.enumeration.DistributorName;
import ai.salesfox.portal.common.exception.PortalException;
import ai.salesfox.portal.common.model.PagedResourceHolder;
import ai.salesfox.portal.common.service.email.EmailMessagingService;
import ai.salesfox.portal.common.service.gift.GiftDetailsService;
import ai.salesfox.portal.database.contact.OrganizationAccountContactEntity;
import ai.salesfox.portal.integration.EmailSubmissionGiftPartner;
import ai.salesfox.portal.integration.noms.configuration.NomsConfiguration;
import ai.salesfox.portal.integration.scribeless.workflow.ScribelessCampaignRequestModelCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class NomsGiftOrderService extends EmailSubmissionGiftPartner {
    private final NomsConfiguration nomsConfiguration;
    private final NomsRecipientCSVGenerator nomsRecipientCSVGenerator;

    @Autowired
    public NomsGiftOrderService(
            GiftDetailsService giftDetailsService,
            ScribelessCampaignRequestModelCreator scribelessCampaignRequestModelCreator,
            EmailMessagingService emailMessagingService,
            NomsConfiguration nomsConfiguration,
            NomsRecipientCSVGenerator nomsRecipientCSVGenerator
    ) {
        super(giftDetailsService, scribelessCampaignRequestModelCreator, emailMessagingService);
        this.nomsConfiguration = nomsConfiguration;
        this.nomsRecipientCSVGenerator = nomsRecipientCSVGenerator;
    }

    @Override
    public DistributorName distributorName() {
        return DistributorName.NOMS;
    }

    @Override
    protected List<String> retrieveOrderEmailAddresses() {
        return List.of(nomsConfiguration.getNomsOrderEmailAddress());
    }

    @Override
    protected File generateRecipientCSVFileToAttach(UUID giftId, PagedResourceHolder<OrganizationAccountContactEntity> contactPageHolder) throws PortalException {
        return nomsRecipientCSVGenerator.createRecipientCSVFile(giftId, contactPageHolder);
    }

}
