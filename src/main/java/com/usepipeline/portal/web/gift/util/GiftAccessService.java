package com.usepipeline.portal.web.gift.util;

import com.usepipeline.portal.common.enumeration.AccessOperation;
import com.usepipeline.portal.common.service.contact.ContactAccessOperationUtility;
import com.usepipeline.portal.database.account.entity.MembershipEntity;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.gift.GiftEntity;
import com.usepipeline.portal.database.organization.account.contact.entity.OrganizationAccountContactEntity;
import com.usepipeline.portal.database.organization.account.contact.repository.OrganizationAccountContactProfileRepository;
import com.usepipeline.portal.database.organization.account.contact.repository.OrganizationAccountContactRepository;
import com.usepipeline.portal.web.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Component
public class GiftAccessService {
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private OrganizationAccountContactRepository contactRepository;
    private ContactAccessOperationUtility<ResponseStatusException> contactAccessOperationUtility;

    @Autowired
    public GiftAccessService(HttpSafeUserMembershipRetrievalService membershipRetrievalService, OrganizationAccountContactRepository contactRepository, OrganizationAccountContactProfileRepository contactProfileRepository) {
        this.membershipRetrievalService = membershipRetrievalService;
        this.contactRepository = contactRepository;
        this.contactAccessOperationUtility = new ContactAccessOperationUtility<>(membershipRetrievalService, contactProfileRepository);
    }

    public void validateGiftAccess(GiftEntity entity, AccessOperation contactAccessOperation) {
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        validateGiftEntityAccess(entity, loggedInUser);
        Optional<OrganizationAccountContactEntity> optionalContact = contactRepository.findById(entity.getContactId());
        if (optionalContact.isPresent()) {
            if (!contactAccessOperationUtility.canUserAccessContact(loggedInUser, optionalContact.get(), contactAccessOperation)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot perform the gifting operation for this contact");
            }
        }
    }

    public void validateUserGiftSendingAccessForContact(UserEntity userRequestingAccess, OrganizationAccountContactEntity contact) {
        if (!contactAccessOperationUtility.canUserAccessContact(userRequestingAccess, contact, AccessOperation.INTERACT)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot send gifts to this contact");
        }
    }

    private void validateGiftEntityAccess(GiftEntity entity, UserEntity loggedInUser) {
        if (membershipRetrievalService.isAuthenticatedUserPipelineAdmin()) {
            return;
        }

        if (membershipRetrievalService.isAuthenticateUserBasicOrPremiumMember()) {
            if (loggedInUser.getUserId().equals(entity.getRequestingUserId())) {
                return;
            }
        } else {
            MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
            if (userMembership.getOrganizationAccountId().equals(entity.getOrganizationAccountId())) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

}
