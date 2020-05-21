package com.usepipeline.portal.web.contact;

import com.usepipeline.portal.common.enumeration.AccessOperation;
import com.usepipeline.portal.common.service.contact.ContactAccessOperationUtility;
import com.usepipeline.portal.common.service.contact.ContactInteractionsUtility;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.organization.account.contact.entity.OrganizationAccountContactEntity;
import com.usepipeline.portal.database.organization.account.contact.repository.OrganizationAccountContactInteractionsRepository;
import com.usepipeline.portal.database.organization.account.contact.repository.OrganizationAccountContactProfileRepository;
import com.usepipeline.portal.database.organization.account.contact.repository.OrganizationAccountContactRepository;
import com.usepipeline.portal.web.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.function.Consumer;

@Component
public class ContactInteractionsService {
    private OrganizationAccountContactRepository contactRepository;
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private ContactAccessOperationUtility<ResponseStatusException> contactAccessOperationUtility;
    private ContactInteractionsUtility<ResponseStatusException> contactInteractionsUtility;

    @Autowired
    public ContactInteractionsService(OrganizationAccountContactRepository contactRepository, OrganizationAccountContactProfileRepository contactProfileRepository,
                                      OrganizationAccountContactInteractionsRepository contactInteractionsRepository, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.contactRepository = contactRepository;
        this.membershipRetrievalService = membershipRetrievalService;
        this.contactAccessOperationUtility = new ContactAccessOperationUtility<>(membershipRetrievalService, contactProfileRepository);
        this.contactInteractionsUtility = new ContactInteractionsUtility<>(membershipRetrievalService, contactInteractionsRepository);
    }

    public void incrementContactInitiations(UUID contactId) {
        increment(contactId, contactInteractionsUtility::incrementContactInitiations);
    }

    public void incrementEngagementsGenerated(UUID contactId) {
        increment(contactId, contactInteractionsUtility::incrementEngagementsGenerated);
    }

    private void increment(UUID contactId, Consumer<OrganizationAccountContactEntity> incrementer) {
        OrganizationAccountContactEntity contactToUpdate = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        boolean canUserUpdateContact = contactAccessOperationUtility.canUserAccessContact(loggedInUser, contactToUpdate, AccessOperation.UPDATE);
        if (!canUserUpdateContact) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        incrementer.accept(contactToUpdate);
    }

}
