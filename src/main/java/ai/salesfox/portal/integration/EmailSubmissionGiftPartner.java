package ai.salesfox.portal.integration;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.integration.scribeless.service.campaign.model.CampaignCreationRequestModel;
import ai.salesfox.portal.common.exception.PortalException;
import ai.salesfox.portal.common.model.PagedResourceHolder;
import ai.salesfox.portal.common.service.email.EmailMessagingService;
import ai.salesfox.portal.common.service.email.PortalEmailException;
import ai.salesfox.portal.common.service.email.model.EmailMessageModel;
import ai.salesfox.portal.common.service.gift.GiftDetailsService;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.catalogue.external.CatalogueItemExternalDetailsEntity;
import ai.salesfox.portal.database.catalogue.item.CatalogueItemEntity;
import ai.salesfox.portal.database.common.AbstractAddressEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactEntity;
import ai.salesfox.portal.database.customization.icon.CustomIconEntity;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.customization.GiftCustomIconDetailEntity;
import ai.salesfox.portal.database.gift.item.GiftItemDetailEntity;
import ai.salesfox.portal.database.gift.mockup.GiftMockupImageEntity;
import ai.salesfox.portal.integration.scribeless.workflow.ScribelessCampaignRequestModelCreator;
import ai.salesfox.portal.integration.scribeless.workflow.model.CampaignCreationRequestHolder;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class EmailSubmissionGiftPartner implements GiftPartner {
    public static final String TEMPLATE_EMAIL_ORDER = "email_order.ftl";

    private static final String ORDER_MESSAGE_LINE_BREAK = "<br />";
    private static final String ORDER_MESSAGE_SECTION_BREAK = "<hr />";

    private final GiftDetailsService giftDetailsService;
    private final ScribelessCampaignRequestModelCreator scribelessCampaignRequestModelCreator;
    private final EmailMessagingService emailMessagingService;

    public EmailSubmissionGiftPartner(
            GiftDetailsService giftDetailsService,
            ScribelessCampaignRequestModelCreator scribelessCampaignRequestModelCreator,
            EmailMessagingService emailMessagingService
    ) {
        this.giftDetailsService = giftDetailsService;
        this.scribelessCampaignRequestModelCreator = scribelessCampaignRequestModelCreator;
        this.emailMessagingService = emailMessagingService;
    }

    @Override
    public void submitGift(GiftEntity gift, UserEntity submittingUser) throws SalesfoxException {
        UUID giftId = gift.getGiftId();
        PagedResourceHolder<OrganizationAccountContactEntity> contactHolder = giftDetailsService.retrieveRecipientHolder(gift);
        File recipientCSVFile = generateRecipientCSVFileToAttach(giftId, contactHolder);

        AbstractAddressEntity returnAddress = giftDetailsService.retrieveReturnAddress(gift);

        String orderMessageBody;
        if (null != gift.getGiftNoteDetailEntity()) {
            CampaignCreationRequestHolder scribelessNoteRequestHolder = scribelessCampaignRequestModelCreator.createRequestHolder(gift);
            CampaignCreationRequestModel scribelessCampaignCreationRequestModel = scribelessNoteRequestHolder.getCampaignCreationRequestModel();
            orderMessageBody = constructOrderMessageBody(gift, returnAddress, scribelessCampaignCreationRequestModel);
        } else {
            orderMessageBody = constructOrderMessageBody(gift, returnAddress);
        }

        EmailMessageModel orderEmailMessage = createOrderEmailMessage(giftId, orderMessageBody);
        try {
            emailMessagingService.sendMessage(orderEmailMessage, List.of(recipientCSVFile));
        } catch (PortalEmailException e) {
            log.error("There was a problem submitting an order with giftId=[{}] to {}: {}.", giftId, distributorName().name(), e.getMessage());
            throw e;
        }
    }

    protected abstract List<String> retrieveOrderEmailAddresses();

    protected abstract File generateRecipientCSVFileToAttach(UUID giftId, PagedResourceHolder<OrganizationAccountContactEntity> contactPageHolder) throws PortalException;

    protected EmailMessageModel createOrderEmailMessage(UUID giftId, String primaryMessage) {
        EmailMessageModel emailMessageModel = new EmailMessageModel(
                retrieveOrderEmailAddresses(),
                "[Salesfox] Gift Order",
                String.format("Gift Order ID: %s", giftId),
                primaryMessage
        );
        emailMessageModel.setTemplateFileName(TEMPLATE_EMAIL_ORDER);
        return emailMessageModel;
    }

    protected String constructOrderMessageBody(GiftEntity gift, AbstractAddressEntity returnAddress, CampaignCreationRequestModel scribelessRequestDetails) {
        String baseOrderMessageBody = constructOrderMessageBody(gift, returnAddress);
        StringBuilder scribelessOrderMessageBody = new StringBuilder(baseOrderMessageBody);

        scribelessOrderMessageBody.append(ORDER_MESSAGE_LINE_BREAK);
        scribelessOrderMessageBody.append(ORDER_MESSAGE_SECTION_BREAK);
        scribelessOrderMessageBody.append(ORDER_MESSAGE_LINE_BREAK);

        scribelessOrderMessageBody.append("Scribeless Campaign Creation Details:");
        scribelessOrderMessageBody.append(ORDER_MESSAGE_LINE_BREAK);

        String scribelessCampaignDetails = scribelessRequestDetails.print(ORDER_MESSAGE_LINE_BREAK);
        scribelessOrderMessageBody.append(scribelessCampaignDetails);

        return scribelessOrderMessageBody.toString();
    }

    protected String constructOrderMessageBody(GiftEntity gift, AbstractAddressEntity returnAddress) {
        StringBuilder orderBodyBuilder = new StringBuilder(ORDER_MESSAGE_LINE_BREAK);
        orderBodyBuilder.append(ORDER_MESSAGE_SECTION_BREAK);
        orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);

        GiftItemDetailEntity giftItemDetail = gift.getGiftItemDetailEntity();
        CatalogueItemEntity catalogItem = giftItemDetail.getCatalogueItemEntity();
        CatalogueItemExternalDetailsEntity externalDetails = catalogItem.getCatalogueItemExternalDetailsEntity();

        orderBodyBuilder.append("Distributor: ");
        orderBodyBuilder.append(distributorName().name());
        orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);
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

        GiftMockupImageEntity giftMockupImage = gift.getGiftMockupImageEntity();
        if (null != giftMockupImage) {
            orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);
            orderBodyBuilder.append("Mockup Image URL: ");
            orderBodyBuilder.append(giftMockupImage.getImageUrl());
        }

        orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);
        orderBodyBuilder.append(ORDER_MESSAGE_SECTION_BREAK);
        orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);

        UserEntity requestingUser = gift.getRequestingUserEntity();
        orderBodyBuilder.append("Ordering User Return Address:");
        orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);
        orderBodyBuilder.append(requestingUser.getFirstName());
        orderBodyBuilder.append(requestingUser.getLastName());
        orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);
        orderBodyBuilder.append(returnAddress.getAddressLine1());
        orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);
        orderBodyBuilder.append(returnAddress.getAddressLine2());
        orderBodyBuilder.append(ORDER_MESSAGE_LINE_BREAK);
        orderBodyBuilder.append(returnAddress.getCity());
        orderBodyBuilder.append(", ");
        orderBodyBuilder.append(returnAddress.getState());
        orderBodyBuilder.append(returnAddress.getZipCode());

        return orderBodyBuilder.toString();
    }

}
