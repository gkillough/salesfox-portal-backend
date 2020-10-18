package ai.salesfox.portal.rest.api.gift.recipient;

import ai.salesfox.portal.common.enumeration.AccessOperation;
import ai.salesfox.portal.common.service.contact.ContactAccessOperationUtility;
import ai.salesfox.portal.common.service.license.UserLicenseLimitManager;
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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GiftRecipientEndpointService {
    private final GiftRepository giftRepository;
    private final GiftRecipientRepository giftRecipientRepository;
    private final OrganizationAccountContactRepository contactRepository;
    private final GiftAccessService giftAccessService;
    private final UserLicenseLimitManager userLicenseLimitManager;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private final ContactAccessOperationUtility contactAccessOperationUtility;

    @Autowired
    public GiftRecipientEndpointService(
            GiftRepository giftRepository,
            GiftRecipientRepository giftRecipientRepository,
            OrganizationAccountContactRepository contactRepository,
            GiftAccessService giftAccessService,
            UserLicenseLimitManager userLicenseLimitManager,
            HttpSafeUserMembershipRetrievalService membershipRetrievalService
    ) {
        this.giftRepository = giftRepository;
        this.giftRecipientRepository = giftRecipientRepository;
        this.contactRepository = contactRepository;
        this.giftAccessService = giftAccessService;
        this.userLicenseLimitManager = userLicenseLimitManager;
        this.membershipRetrievalService = membershipRetrievalService;
        this.contactAccessOperationUtility = new ContactAccessOperationUtility(contactRepository);
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
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        findExistingGiftAndValidateInteraction(loggedInUser, giftId, recipientRequest);
        validateRecipientCount(loggedInUser, recipientRequest.getContactIds().size());

        giftRecipientRepository.deleteByGiftId(giftId);
        List<GiftRecipientEntity> giftRecipients = createGiftRecipientEntities(giftId, recipientRequest.getContactIds());
        giftRecipientRepository.saveAll(giftRecipients);
    }

    @Transactional
    public void appendRecipients(UUID giftId, GiftRecipientRequestModel recipientRequest) {
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        findExistingGiftAndValidateInteraction(loggedInUser, giftId, recipientRequest);

        int newRecipientCount = recipientRequest.getContactIds().size();
        int existingRecipientCount = giftRecipientRepository.countByGiftId(giftId);
        validateRecipientCount(loggedInUser, newRecipientCount + existingRecipientCount);

        List<GiftRecipientEntity> giftRecipients = createGiftRecipientEntities(giftId, recipientRequest.getContactIds());
        giftRecipientRepository.saveAll(giftRecipients);
    }

    @Transactional
    public void deleteRecipients(UUID giftId, GiftRecipientRequestModel recipientRequest) {
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        findExistingGiftAndValidateInteraction(loggedInUser, giftId, recipientRequest);

        List<GiftRecipientEntity> giftRecipientEntitiesToDelete = createGiftRecipientEntities(giftId, recipientRequest.getContactIds());
        giftRecipientRepository.deleteInBatch(giftRecipientEntitiesToDelete);
    }

    private GiftEntity findExistingGift(UUID giftId) {
        return giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void findExistingGiftAndValidateInteraction(UserEntity loggedInUser, UUID giftId, GiftRecipientRequestModel requestModel) {
        GiftEntity foundGift = findExistingGift(giftId);
        if (!foundGift.isSubmittable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot modify the recipients of a gift that has been submitted");
        }

        giftAccessService.validateGiftEntityAccess(foundGift, loggedInUser);

        List<UUID> requestedContactIds = requestModel.getContactIds();
        if (null == requestedContactIds || requestedContactIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'contactIds' is required and must not be empty");
        }

        Set<UUID> uniqueContactIds = new LinkedHashSet<>(requestedContactIds);
        if (uniqueContactIds.size() != requestedContactIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The contact IDs provided are not unique");
        }

        if (!contactAccessOperationUtility.canUserAccessContacts(loggedInUser, uniqueContactIds, AccessOperation.INTERACT)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not all contacts provided are accessible by this user");
        }
    }

    private void validateRecipientCount(UserEntity loggedInUser, int totalResultingRecipients) {
        int contactPerCampaignLimit = userLicenseLimitManager.retrieveContactPerCampaignLimit(loggedInUser);
        if (totalResultingRecipients > contactPerCampaignLimit) {
            throw new ResponseStatusException(
                    HttpStatus.PAYMENT_REQUIRED,
                    String.format("The number of submitted recipients would cause you to exceed the allowed number of recipients-per-campaign, which is [%d], for this organization account's license.", contactPerCampaignLimit)
            );
        }
    }

    private List<GiftRecipientEntity> createGiftRecipientEntities(UUID giftId, List<UUID> contactIds) {
        return contactIds
                .stream()
                .map(contactId -> new GiftRecipientEntity(giftId, contactId))
                .collect(Collectors.toList());
    }

}
