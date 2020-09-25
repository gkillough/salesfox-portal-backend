package ai.salesfox.portal.integration.scribeless.workflow;

import ai.salesfox.integration.scribeless.model.ScribelessAddressModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignCreationRequestModel;
import ai.salesfox.portal.database.contact.OrganizationAccountContactEntity;
import ai.salesfox.portal.database.contact.address.OrganizationAccountContactAddressEntity;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.recipient.GiftRecipientRepository;
import ai.salesfox.portal.database.note.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScribelessCampaignRequestModelCreator {
    public static final String DEFAULT_ADDRESS_COUNTRY = "USA";

    private final GiftRecipientRepository giftRecipientRepository;
    private final NoteRepository noteRepository;

    @Autowired
    public ScribelessCampaignRequestModelCreator(GiftRecipientRepository giftRecipientRepository, NoteRepository noteRepository) {
        this.giftRecipientRepository = giftRecipientRepository;
        this.noteRepository = noteRepository;
    }

    public CampaignCreationRequestModel createRequestModel(GiftEntity gift) {
        // FIXME implement
        return null;
    }

    public ScribelessAddressModel createAddressModel(OrganizationAccountContactEntity contact) {
        OrganizationAccountContactAddressEntity address = contact.getContactAddressEntity();
        return new ScribelessAddressModel(
                null,
                contact.getFirstName(),
                contact.getLastName(),
                null,
                address.getAddressLine1(),
                address.getAddressLine2(),
                null,
                address.getCity(),
                DEFAULT_ADDRESS_COUNTRY,
                address.getState(),
                address.getZipCode()
        );
    }

}
