package ai.salesfox.portal.rest.api.gift.recipient;

import ai.salesfox.portal.common.enumeration.AccessOperation;
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

    @Autowired
    public GiftRecipientEndpointService(GiftRepository giftRepository, GiftRecipientRepository giftRecipientRepository, OrganizationAccountContactRepository contactRepository, GiftAccessService giftAccessService, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.giftRepository = giftRepository;
        this.giftRecipientRepository = giftRecipientRepository;
        this.contactRepository = contactRepository;
        this.giftAccessService = giftAccessService;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public MultiGiftRecipientResponseModel getRecipients(UUID giftId, Integer pageOffset, Integer pageLimit) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);
        GiftEntity foundGift = findExistingGift(giftId);
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        giftAccessService.validateGiftAccess(foundGift, loggedInUser, AccessOperation.READ);

        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        Page<UUID> giftRecipientIds = giftRecipientRepository.findByGiftId(giftId, pageRequest).map(GiftRecipientEntity::getContactId);
        List<ContactSummaryModel> recipientModels = contactRepository.findAllById(giftRecipientIds)
                .stream()
                .map(ContactSummaryModel::fromEntity)
                .collect(Collectors.toList());
        return new MultiGiftRecipientResponseModel(recipientModels, giftRecipientIds);
    }

    public void setRecipients(UUID giftId, GiftRecipientRequestModel recipientRequest) {
        // FIXME implement
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    public void appendRecipients(UUID giftId, GiftRecipientRequestModel recipientRequest) {
        // FIXME implement
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @Transactional
    public void deleteRecipients(UUID giftId, GiftRecipientRequestModel recipientRequest) {
        GiftEntity foundGift = findExistingGift(giftId);
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        giftAccessService.validateGiftAccess(foundGift, loggedInUser, AccessOperation.INTERACT);

        List<GiftRecipientEntity> giftRecipientEntitiesToDelete = recipientRequest.getContactIds()
                .stream()
                .map(contactId -> new GiftRecipientEntity(giftId, contactId))
                .collect(Collectors.toList());
        giftRecipientRepository.deleteInBatch(giftRecipientEntitiesToDelete);
    }

    private GiftEntity findExistingGift(UUID giftId) {
        return giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

}
