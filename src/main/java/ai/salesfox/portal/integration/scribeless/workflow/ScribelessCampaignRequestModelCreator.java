package ai.salesfox.portal.integration.scribeless.workflow;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.integration.scribeless.enumeration.ScribelessProductType;
import ai.salesfox.integration.scribeless.model.ScribelessAddressModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignCreationRequestModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignUpdateRequestModel;
import ai.salesfox.portal.common.FieldValidationUtils;
import ai.salesfox.portal.common.model.PortalAddressModel;
import ai.salesfox.portal.database.account.entity.UserAddressEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.account.repository.UserAddressRepository;
import ai.salesfox.portal.database.common.AbstractAddressEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactRepository;
import ai.salesfox.portal.database.contact.profile.OrganizationAccountContactProfileEntity;
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

import javax.annotation.Nullable;
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
    private final UserAddressRepository userAddressRepository;

    @Autowired
    public ScribelessCampaignRequestModelCreator(GiftRecipientRepository giftRecipientRepository, OrganizationAccountContactRepository contactRepository, NoteRepository noteRepository,
                                                 CustomBrandingTextRepository customBrandingTextRepository, CustomIconRepository customIconRepository, UserAddressRepository userAddressRepository) {
        this.giftRecipientRepository = giftRecipientRepository;
        this.contactRepository = contactRepository;
        this.noteRepository = noteRepository;
        this.customBrandingTextRepository = customBrandingTextRepository;
        this.customIconRepository = customIconRepository;
        this.userAddressRepository = userAddressRepository;
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
            footerFont = ScribelessCampaignDefaults.DEFAULT_FOOTER_FONT;
        }

        String headerImageUrl = null;
        String headerType = null;
        Optional<String> optionalIcon = retrieveCustomIconUrl(gift);
        if (optionalIcon.isPresent()) {
            headerImageUrl = optionalIcon.get();
            headerType = ScribelessCampaignDefaults.DEFAULT_HEADER_TYPE;
        }

        ScribelessAddressModel returnAddress = retrieveReturnAddress(gift);

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

        Page<GiftRecipientEntity> firstPageOfRecipients = giftRecipientRepository.findByGiftId(giftId, DEFAULT_PAGE_REQUEST);
        return new CampaignCreationRequestHolder(creationRequestModel, firstPageOfRecipients, pageRequest -> giftRecipientRepository.findByGiftId(giftId, pageRequest));
    }

    // TODO consider validating addresses before creating an update request model and emailing the gift creator
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

    private ScribelessAddressModel retrieveReturnAddress(GiftEntity gift) throws SalesfoxException {
        UserEntity requestingUser = gift.getRequestingUserEntity();
        UserAddressEntity requestingUserAddress = userAddressRepository.findById(requestingUser.getUserId())
                .filter(this::isValidAddress)
                .orElseThrow(() -> new SalesfoxException("The requesting Salesfox user does not have an address"));
        return createAddressModel(requestingUser.getFirstName(), requestingUser.getLastName(), requestingUserAddress);
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

    private Optional<String> retrieveCustomIconUrl(GiftEntity gift) {
        GiftCustomIconDetailEntity giftCustomIconDetail = gift.getGiftCustomIconDetailEntity();
        if (null == giftCustomIconDetail) {
            return Optional.empty();
        }
        return customIconRepository.findById(giftCustomIconDetail.getCustomIconId())
                .map(CustomIconEntity::getIconUrl);
    }

    private boolean isValidAddress(AbstractAddressEntity addressEntity) {
        PortalAddressModel portalAddressModel = PortalAddressModel.fromEntity(addressEntity);
        return FieldValidationUtils.isValidUSAddress(portalAddressModel, false);
    }

}
