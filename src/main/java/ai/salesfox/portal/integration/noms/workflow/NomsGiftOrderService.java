package ai.salesfox.portal.integration.noms.workflow;

import ai.salesfox.portal.common.enumeration.DistributorName;
import ai.salesfox.portal.common.service.email.EmailMessagingService;
import ai.salesfox.portal.common.service.email.model.EmailMessageModel;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.integration.GiftPartner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NomsGiftOrderService implements GiftPartner {
    private final NomsRecipientCSVGenerator nomsRecipientCSVGenerator;
    private final EmailMessagingService emailMessagingService;

    @Autowired
    public NomsGiftOrderService(NomsRecipientCSVGenerator nomsRecipientCSVGenerator, EmailMessagingService emailMessagingService) {
        this.nomsRecipientCSVGenerator = nomsRecipientCSVGenerator;
        this.emailMessagingService = emailMessagingService;
    }

    @Override
    public DistributorName distributorName() {
        return DistributorName.NOMS;
    }

    // FIXME add Scribeless note details
    @Override
    public void submitGift(GiftEntity gift, UserEntity submittingUser) {
        // FIXME implement
    }

    private EmailMessageModel createOrderEmailMessage() {
        // FIXME implement
        return null;
    }

}
