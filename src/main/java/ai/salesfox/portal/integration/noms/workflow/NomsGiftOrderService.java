package ai.salesfox.portal.integration.noms.workflow;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.integration.scribeless.model.ScribelessAddressModel;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignCreationRequestModel;
import ai.salesfox.portal.common.enumeration.DistributorName;
import ai.salesfox.portal.common.model.PagedResourceHolder;
import ai.salesfox.portal.common.service.email.EmailMessagingService;
import ai.salesfox.portal.common.service.email.PortalEmailException;
import ai.salesfox.portal.common.service.email.model.EmailMessageModel;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.catalogue.external.CatalogueItemExternalDetailsEntity;
import ai.salesfox.portal.database.catalogue.item.CatalogueItemEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactRepository;
import ai.salesfox.portal.database.customization.icon.CustomIconEntity;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.customization.GiftCustomIconDetailEntity;
import ai.salesfox.portal.database.gift.item.GiftItemDetailEntity;
import ai.salesfox.portal.database.gift.recipient.GiftRecipientEntity;
import ai.salesfox.portal.integration.GiftPartner;
import ai.salesfox.portal.integration.noms.configuration.NomsConfiguration;
import ai.salesfox.portal.integration.scribeless.workflow.ScribelessCampaignRequestModelCreator;
import ai.salesfox.portal.integration.scribeless.workflow.model.CampaignCreationRequestHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class NomsGiftOrderService implements GiftPartner {
    private static final String ORDER_MESSAGE_LINE_BREAK = "<br />";
    private static final String ORDER_MESSAGE_SECTION_BREAK = "<hr />";

    private final NomsConfiguration nomsConfiguration;
    private final NomsRecipientCSVGenerator nomsRecipientCSVGenerator;
    private final ScribelessCampaignRequestModelCreator scribelessCampaignRequestModelCreator;
    private final OrganizationAccountContactRepository contactRepository;
    private final EmailMessagingService emailMessagingService;

    @Autowired
    public NomsGiftOrderService(
            NomsConfiguration nomsConfiguration,
            NomsRecipientCSVGenerator nomsRecipientCSVGenerator,
            ScribelessCampaignRequestModelCreator scribelessCampaignRequestModelCreator,
            OrganizationAccountContactRepository contactRepository, EmailMessagingService emailMessagingService
    ) {
        this.nomsConfiguration = nomsConfiguration;
        this.nomsRecipientCSVGenerator = nomsRecipientCSVGenerator;
        this.scribelessCampaignRequestModelCreator = scribelessCampaignRequestModelCreator;
        this.contactRepository = contactRepository;
        this.emailMessagingService = emailMessagingService;
    }

    @Override
    public DistributorName distributorName() {
        return DistributorName.NOMS;
    }

    @Override
    // TODO it would be nice to keep the Scribeless details completely separate
    public void submitGift(GiftEntity gift, UserEntity submittingUser) throws SalesfoxException {

        // FIXME figure out what to do if no scribeless note present

        CampaignCreationRequestHolder scribelessNoteRequestHolder = scribelessCampaignRequestModelCreator.createRequestHolder(gift);
        CampaignCreationRequestModel scribelessCampaignCreationRequestModel = scribelessNoteRequestHolder.getCampaignCreationRequestModel();

        PagedResourceHolder<OrganizationAccountContactEntity> contactHolder = scribelessNoteRequestHolder.mapPage(this::convertGiftRecipientsToContacts);
        File recipientCSVFile = nomsRecipientCSVGenerator.createRecipientCSVFile(gift.getGiftId(), contactHolder);

        String orderMessageBody = constructOrderMessageBody(gift, scribelessCampaignCreationRequestModel);
        EmailMessageModel orderEmailMessage = createOrderEmailMessage(gift.getGiftId(), orderMessageBody);
        try {
            emailMessagingService.sendMessage(orderEmailMessage, List.of(recipientCSVFile));
        } catch (PortalEmailException e) {
            log.error("There was a problem submitting an order to NOMS", e);
        }
    }

    private Page<OrganizationAccountContactEntity> convertGiftRecipientsToContacts(Page<GiftRecipientEntity> giftRecipientPage) {
        List<UUID> contactIds = giftRecipientPage
                .stream()
                .map(GiftRecipientEntity::getContactId)
                .collect(Collectors.toList());
        List<OrganizationAccountContactEntity> contacts = contactRepository.findAllById(contactIds);
        return new PageImpl<>(contacts, giftRecipientPage.getPageable(), giftRecipientPage.getTotalElements());
    }

    private EmailMessageModel createOrderEmailMessage(UUID giftId, String primaryMessage) {
        EmailMessageModel emailMessageModel = new EmailMessageModel(
                List.of(nomsConfiguration.getNomsOrderEmailAddress()),
                "[Salesfox] Gift Order",
                String.format("Gift Order ID: %s", giftId),
                primaryMessage
        );
        emailMessageModel.setTemplateFileName(NomsConfiguration.NOMS_ORDER_EMAIL_TEMPLATE);
        return emailMessageModel;
    }

    // TODO extract this into common class
    private String constructOrderMessageBody(GiftEntity gift, CampaignCreationRequestModel scribelessRequestDetails) {
        StringBuilder orderBodyBuilder = new StringBuilder(ORDER_MESSAGE_LINE_BREAK);
        orderBodyBuilder.append(ORDER_MESSAGE_SECTION_BREAK);
        orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);

        GiftItemDetailEntity giftItemDetail = gift.getGiftItemDetailEntity();
        CatalogueItemEntity catalogItem = giftItemDetail.getCatalogueItemEntity();
        CatalogueItemExternalDetailsEntity externalDetails = catalogItem.getCatalogueItemExternalDetailsEntity();

        orderBodyBuilder.append(distributorName());
        orderBodyBuilder.append(" Order Details:");
        orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);
        orderBodyBuilder.append("Item Name: ");
        orderBodyBuilder.append(catalogItem.getName());
        orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);
        orderBodyBuilder.append("External ID: ");
        orderBodyBuilder.append(externalDetails.getExternalId());

        GiftCustomIconDetailEntity customIconDetail = gift.getGiftCustomIconDetailEntity();
        if (null != customIconDetail) {
            CustomIconEntity customIcon = customIconDetail.getCustomIconEntity();
            orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);
            orderBodyBuilder.append("Custom Image URL: ");
            orderBodyBuilder.append(customIcon.getIconUrl());
        }

        orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);
        orderBodyBuilder.append(ORDER_MESSAGE_SECTION_BREAK);
        orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);

        ScribelessAddressModel returnAddress = scribelessRequestDetails.getReturnAddress();
        orderBodyBuilder.append("Ordering User Return Address:");
        orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);
        orderBodyBuilder.append(returnAddress.getFirstName());
        orderBodyBuilder.append(returnAddress.getLastName());
        orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);
        orderBodyBuilder.append(returnAddress.getAddressLine1());
        orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);
        orderBodyBuilder.append(returnAddress.getAddressLine2());
        orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);
        orderBodyBuilder.append(returnAddress.getCity());
        orderBodyBuilder.append(", ");
        orderBodyBuilder.append(returnAddress.getStateRegion());
        orderBodyBuilder.append(returnAddress.getZipPostalCode());

        orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);
        orderBodyBuilder.append(ORDER_MESSAGE_SECTION_BREAK);
        orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);

        orderBodyBuilder.append("Scribeless Campaign Creation Details:");
        orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);

        String scribelessCampaignDetails = scribelessRequestDetails.print(ORDER_MESSAGE_LINE_BREAK);
        orderBodyBuilder.append(scribelessCampaignDetails);

        return orderBodyBuilder.toString();
    }

}
