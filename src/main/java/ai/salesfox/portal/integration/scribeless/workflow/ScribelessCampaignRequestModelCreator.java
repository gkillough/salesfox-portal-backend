package ai.salesfox.portal.integration.scribeless.workflow;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.integration.scribeless.enumeration.ScribelessProductType;
import ai.salesfox.integration.scribeless.model.ScribelessAddressModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignCreationRequestModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignUpdateRequestModel;
import ai.salesfox.portal.database.common.AbstractAddressEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactRepository;
import ai.salesfox.portal.database.customization.branding_text.CustomBrandingTextEntity;
import ai.salesfox.portal.database.customization.branding_text.CustomBrandingTextRepository;
import ai.salesfox.portal.database.customization.icon.CustomIconEntity;
import ai.salesfox.portal.database.customization.icon.CustomIconRepository;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.customization.GiftCustomIconDetailEntity;
import ai.salesfox.portal.database.gift.customization.GiftCustomTextDetailEntity;
import ai.salesfox.portal.database.gift.note.GiftNoteDetailEntity;
import ai.salesfox.portal.database.gift.recipient.GiftRecipientEntity;
import ai.salesfox.portal.database.gift.recipient.GiftRecipientRepository;
import ai.salesfox.portal.database.note.NoteEntity;
import ai.salesfox.portal.database.note.NoteRepository;
import ai.salesfox.portal.integration.scribeless.workflow.model.CampaignCreationRequestHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ScribelessCampaignRequestModelCreator {
    public static final PageRequest DEFAULT_PAGE_REQUEST = PageRequest.of(0, 500);

    private final GiftRecipientRepository giftRecipientRepository;
    private final OrganizationAccountContactRepository contactRepository;
    private final NoteRepository noteRepository;
    private final CustomBrandingTextRepository customBrandingTextRepository;
    private final CustomIconRepository customIconRepository;

    @Autowired
    public ScribelessCampaignRequestModelCreator(GiftRecipientRepository giftRecipientRepository, OrganizationAccountContactRepository contactRepository, NoteRepository noteRepository,
                                                 CustomBrandingTextRepository customBrandingTextRepository, CustomIconRepository customIconRepository) {
        this.giftRecipientRepository = giftRecipientRepository;
        this.contactRepository = contactRepository;
        this.noteRepository = noteRepository;
        this.customBrandingTextRepository = customBrandingTextRepository;
        this.customIconRepository = customIconRepository;
    }

    public CampaignCreationRequestHolder createRequestHolder(GiftEntity gift) throws SalesfoxException {
        UUID giftId = gift.getGiftId();
        NoteEntity giftNote = retrieveNote(gift);

        String footerText = null;
        String footerFont = null;
        Optional<String> optionalBrandingText = retrieveCustomText(gift)
                .map(CustomBrandingTextEntity::getCustomBrandingText);
        if (optionalBrandingText.isPresent()) {
            footerText = optionalBrandingText.get();
            footerFont = "Arial";
        }

        String headerImageUrl = null;
        String headerType = null;
        Optional<CustomIconEntity> optionalIcon = retrieveCustomIcon(gift);
        if (optionalIcon.isPresent()) {
            headerImageUrl = retrieveCustomIconUrl(optionalIcon.get());
            headerType = ScribelessCampaignDefaults.DEFAULT_HEADER_TYPE; // TODO determine if this is correct
        }

        // TODO determine valid defaults for these
        CampaignCreationRequestModel creationRequestModel = new CampaignCreationRequestModel(
                ScribelessCampaignDefaults.DEFAULT_PAPER_SIZE_USA,
                giftNote.getHandwritingStyle(),
                giftNote.getFontColor(),
                null, // TODO replace stored "font size" with a Small, Medium, or Large string
                "Salesfox Gift Id: " + giftId,
                ScribelessProductType.ON_DEMAND.getText(),
                giftNote.getMessage(),
                null,
                null,
                null, // TODO maybe this is where distributor info will go?
                headerImageUrl,
                headerType,
                null,
                null,
                footerText,
                footerFont,
                null,
                List.of()
        );

        Page<GiftRecipientEntity> firstPageOfRecipients = giftRecipientRepository.findByGiftId(giftId, DEFAULT_PAGE_REQUEST);
        return new CampaignCreationRequestHolder(creationRequestModel, firstPageOfRecipients, pageRequest -> giftRecipientRepository.findByGiftId(giftId, pageRequest));
    }

    public CampaignUpdateRequestModel createUpdateRequestModel(Streamable<GiftRecipientEntity> giftRecipients) {
        Set<UUID> contactIds = giftRecipients
                .stream()
                .map(GiftRecipientEntity::getContactId)
                .collect(Collectors.toSet());
        List<ScribelessAddressModel> recipientAddresses = contactRepository.findAllById(contactIds)
                .stream()
                .map(this::createAddressModel)
                .collect(Collectors.toList());
        return new CampaignUpdateRequestModel(recipientAddresses);
    }

    public ScribelessAddressModel createAddressModel(OrganizationAccountContactEntity contact) {
        return createAddressModel(contact.getFirstName(), contact.getLastName(), contact.getContactAddressEntity());
    }

    public ScribelessAddressModel createAddressModel(String firstName, String lastName, AbstractAddressEntity address) {
        return new ScribelessAddressModel(
                null,
                firstName,
                lastName,
                null,
                address.getAddressLine1(),
                address.getAddressLine2(),
                null,
                address.getCity(),
                ScribelessCampaignDefaults.DEFAULT_COUNTRY,
                address.getState(),
                address.getZipCode()
        );
    }

    private NoteEntity retrieveNote(GiftEntity gift) throws SalesfoxException {
        GiftNoteDetailEntity giftNoteDetail = gift.getGiftNoteDetailEntity();
        if (null == giftNoteDetail) {
            throw new SalesfoxException(String.format("The requested gift with id=[%s] has no note attached to it", gift.getGiftId()));
        }

        return noteRepository.findById(giftNoteDetail.getNoteId())
                .orElseThrow(() -> new SalesfoxException(String.format("The requested gift with id=[%s] has a note attachment, but none existed in the database", gift.getGiftId())));
    }

    private Optional<CustomBrandingTextEntity> retrieveCustomText(GiftEntity gift) {
        GiftCustomTextDetailEntity giftCustomTextDetail = gift.getGiftCustomTextDetailEntity();
        if (null == giftCustomTextDetail) {
            return Optional.empty();
        }
        return customBrandingTextRepository.findById(giftCustomTextDetail.getCustomTextId());
    }

    private Optional<CustomIconEntity> retrieveCustomIcon(GiftEntity gift) {
        GiftCustomIconDetailEntity giftCustomIconDetail = gift.getGiftCustomIconDetailEntity();
        if (null == giftCustomIconDetail) {
            return Optional.empty();
        }
        return customIconRepository.findById(giftCustomIconDetail.getCustomIconId());
    }

    private String retrieveCustomIconUrl(CustomIconEntity customIcon) {
        // FIXME update this when it is a url
        return null;
    }

}
