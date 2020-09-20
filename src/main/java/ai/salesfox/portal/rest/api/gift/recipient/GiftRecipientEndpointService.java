package ai.salesfox.portal.rest.api.gift.recipient;

import ai.salesfox.portal.common.enumeration.AccessOperation;
import ai.salesfox.portal.common.service.contact.ContactAccessOperationUtility;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactRepository;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.GiftRepository;
import ai.salesfox.portal.database.gift.recipient.GiftRecipientEntity;
import ai.salesfox.portal.database.gift.recipient.GiftRecipientRepository;
import ai.salesfox.portal.rest.api.common.page.PageRequestValidationUtils;
import ai.salesfox.portal.rest.api.contact.model.ContactSummaryModel;
import ai.salesfox.portal.rest.api.gift.util.GiftAccessService;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GiftRecipientEndpointService {
    private final GiftRepository giftRepository;
    private final GiftRecipientRepository giftRecipientRepository;
    private final OrganizationAccountContactRepository contactRepository;
    private final GiftAccessService giftAccessService;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private final ContactAccessOperationUtility<ResponseStatusException> contactAccessOperationUtility;

    @Autowired
    public GiftRecipientEndpointService(GiftRepository giftRepository, GiftRecipientRepository giftRecipientRepository, OrganizationAccountContactRepository contactRepository, GiftAccessService giftAccessService, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.giftRepository = giftRepository;
        this.giftRecipientRepository = giftRecipientRepository;
        this.contactRepository = contactRepository;
        this.giftAccessService = giftAccessService;
        this.membershipRetrievalService = membershipRetrievalService;
        this.contactAccessOperationUtility = new ContactAccessOperationUtility<>(contactRepository);
    }

    public MultiGiftRecipientResponseModel getRecipients(UUID giftId, Integer pageOffset, Integer pageLimit) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);
        GiftEntity foundGift = findExistingGift(giftId);

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        giftAccessService.validateGiftEntityAccess(foundGift, loggedInUser);

        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        Page<UUID> giftRecipientIds = giftRecipientRepository.findByGiftId(giftId, pageRequest).map(GiftRecipientEntity::getContactId);
        if (giftRecipientIds.isEmpty()) {
            return MultiGiftRecipientResponseModel.empty();
        }

        List<ContactSummaryModel> recipientModels = contactRepository.findAllById(giftRecipientIds)
                .stream()
                .map(ContactSummaryModel::fromEntity)
                .collect(Collectors.toList());
        return new MultiGiftRecipientResponseModel(recipientModels, giftRecipientIds);
    }

    @Transactional
    public void setRecipients(UUID giftId, GiftRecipientRequestModel recipientRequest) {
        findExistingGiftAndValidateInteraction(giftId, recipientRequest);

        giftRecipientRepository.deleteByGiftId(giftId);
        List<GiftRecipientEntity> giftRecipients = createGiftRecipientEntities(giftId, recipientRequest.getContactIds());
        giftRecipientRepository.saveAll(giftRecipients);
    }

    @Transactional
    public void appendRecipients(UUID giftId, GiftRecipientRequestModel recipientRequest) {
        findExistingGiftAndValidateInteraction(giftId, recipientRequest);

        List<GiftRecipientEntity> giftRecipients = createGiftRecipientEntities(giftId, recipientRequest.getContactIds());
        giftRecipientRepository.saveAll(giftRecipients);
    }

    @Transactional
    public void deleteRecipients(UUID giftId, GiftRecipientRequestModel recipientRequest) {
        findExistingGiftAndValidateInteraction(giftId, recipientRequest);

        List<GiftRecipientEntity> giftRecipientEntitiesToDelete = createGiftRecipientEntities(giftId, recipientRequest.getContactIds());
        giftRecipientRepository.deleteInBatch(giftRecipientEntitiesToDelete);
    }

    private GiftEntity findExistingGift(UUID giftId) {
        return giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void findExistingGiftAndValidateInteraction(UUID giftId, GiftRecipientRequestModel requestModel) {
        GiftEntity foundGift = findExistingGift(giftId);
        if (!foundGift.isSubmittable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot modify the recipients of a gift that has been submitted");
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        giftAccessService.validateGiftEntityAccess(foundGift, loggedInUser);

        List<UUID> requestedContactIds = requestModel.getContactIds();
        if (null == requestedContactIds || requestedContactIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'contactIds' is required and must not be empty");
        }

        if (!contactAccessOperationUtility.canUserAccessContacts(loggedInUser, requestedContactIds, AccessOperation.INTERACT)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not all contacts provided are accessible by this user");
        }
    }

    private List<GiftRecipientEntity> createGiftRecipientEntities(UUID giftId, List<UUID> contactIds) {
        return contactIds
                .stream()
                .map(contactId -> new GiftRecipientEntity(giftId, contactId))
                .collect(Collectors.toList());
    }

}
