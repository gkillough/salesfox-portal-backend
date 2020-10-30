package ai.salesfox.portal.integration.scribeless.workflow;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.integration.scribeless.enumeration.ScribelessProductType;
import ai.salesfox.integration.scribeless.model.ScribelessAddressModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignCreationRequestModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignUpdateRequestModel;
import ai.salesfox.portal.common.model.PagedResourceHolder;
import ai.salesfox.portal.common.service.gift.GiftDetailsService;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.common.AbstractAddressEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactEntity;
import ai.salesfox.portal.database.contact.profile.OrganizationAccountContactProfileEntity;
import ai.salesfox.portal.database.customization.branding_text.CustomBrandingTextEntity;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.note.GiftNoteDetailEntity;
import ai.salesfox.portal.database.note.NoteEntity;
import ai.salesfox.portal.database.note.NoteRepository;
import ai.salesfox.portal.integration.scribeless.workflow.model.CampaignCreationRequestHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ScribelessCampaignRequestModelCreator {
    private final GiftDetailsService giftDetailsService;
    private final NoteRepository noteRepository;

    @Autowired
    public ScribelessCampaignRequestModelCreator(GiftDetailsService giftDetailsService, NoteRepository noteRepository) {
        this.giftDetailsService = giftDetailsService;
        this.noteRepository = noteRepository;
    }

    public CampaignCreationRequestHolder createRequestHolder(GiftEntity gift) throws SalesfoxException {
        UUID giftId = gift.getGiftId();
        NoteEntity giftNote = retrieveNote(gift);

        String footerText = null;
        String footerFont = null;
        Optional<String> optionalBrandingText = giftDetailsService.retrieveCustomText(gift)
                .map(CustomBrandingTextEntity::getCustomBrandingText);
        if (optionalBrandingText.isPresent()) {
            footerText = optionalBrandingText.get();
            footerFont = ScribelessCampaignDefaults.DEFAULT_FOOTER_FONT;
        }

        String headerImageUrl = null;
        String headerType = null;
        Optional<String> optionalIcon = giftDetailsService.retrieveCustomIconUrl(gift);
        if (optionalIcon.isPresent()) {
            headerImageUrl = optionalIcon.get();
            headerType = ScribelessCampaignDefaults.DEFAULT_HEADER_TYPE;
        }

        ScribelessAddressModel returnAddress = retrieveScribelessReturnAddress(gift);

        CampaignCreationRequestModel creationRequestModel = new CampaignCreationRequestModel(
                ScribelessCampaignDefaults.DEFAULT_PAPER_SIZE_USA,
                giftNote.getHandwritingStyle(),
                giftNote.getFontColor(),
                giftNote.getFontSize(),
                "Salesfox Gift Id: " + giftId,
                ScribelessProductType.FULL_SERVICE.getText(),
                giftNote.getMessage(),
                null,
                null,
                null,
                headerImageUrl,
                headerType,
                null,
                null,
                footerText,
                footerFont,
                ScribelessCampaignDefaults.DEFAULT_DELIVERY_MODEL,
                returnAddress,
                List.of()
        );

        PagedResourceHolder<OrganizationAccountContactEntity> recipientHolder = giftDetailsService.retrieveRecipientHolder(gift);
        return new CampaignCreationRequestHolder(creationRequestModel, recipientHolder);
    }

    // TODO consider validating addresses before creating an update request model and emailing the gift creator
    public CampaignUpdateRequestModel createUpdateRequestModel(Streamable<OrganizationAccountContactEntity> giftRecipients) {
        List<ScribelessAddressModel> recipientAddresses = giftRecipients
                .stream()
                .map(this::createAddressModel)
                .collect(Collectors.toList());
        return new CampaignUpdateRequestModel(recipientAddresses);
    }

    public ScribelessAddressModel createAddressModel(OrganizationAccountContactEntity contact) {
        String title = Optional.ofNullable(contact.getContactProfileEntity()).map(OrganizationAccountContactProfileEntity::getTitle).orElse(null);
        return createAddressModel(contact.getFirstName(), contact.getLastName(), contact.getContactAddressEntity(), title);
    }

    public ScribelessAddressModel createAddressModel(String firstName, String lastName, AbstractAddressEntity address) {
        return createAddressModel(firstName, lastName, address, null);
    }

    public ScribelessAddressModel createAddressModel(String firstName, String lastName, AbstractAddressEntity address, @Nullable String title) {
        return new ScribelessAddressModel(
                title,
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

    private ScribelessAddressModel retrieveScribelessReturnAddress(GiftEntity gift) throws SalesfoxException {
        UserEntity requestingUser = gift.getRequestingUserEntity();
        AbstractAddressEntity addressEntity = giftDetailsService.retrieveReturnAddress(gift);
        return createAddressModel(requestingUser.getFirstName(), requestingUser.getLastName(), addressEntity);
    }

    private NoteEntity retrieveNote(GiftEntity gift) throws SalesfoxException {
        GiftNoteDetailEntity giftNoteDetail = gift.getGiftNoteDetailEntity();
        if (null == giftNoteDetail) {
            throw new SalesfoxException(String.format("The requested gift with id=[%s] has no note attached to it", gift.getGiftId()));
        }

        return noteRepository.findById(giftNoteDetail.getNoteId())
                .orElseThrow(() -> new SalesfoxException(String.format("The requested gift with id=[%s] has a note attachment, but none existed in the database", gift.getGiftId())));
    }

}
