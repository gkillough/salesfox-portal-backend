package com.getboostr.portal.rest.api.contact.interaction;

import com.getboostr.portal.common.service.contact.ContactAccessOperationUtility;
import com.getboostr.portal.common.service.contact.ContactInteractionsUtility;
import com.getboostr.portal.database.contact.OrganizationAccountContactRepository;
import com.getboostr.portal.database.contact.interaction.ContactInteractionRepository;
import com.getboostr.portal.database.contact.profile.OrganizationAccountContactProfileRepository;
import com.getboostr.portal.rest.api.contact.interaction.model.ContactInteractionRequestModel;
import com.getboostr.portal.rest.api.contact.interaction.model.MultiInteractionModel;
import com.getboostr.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.UUID;

@Component
public class ContactInteractionService {
    private final OrganizationAccountContactRepository contactRepository;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private final ContactAccessOperationUtility<ResponseStatusException> contactAccessOperationUtility;
    private final ContactInteractionsUtility<ResponseStatusException> contactInteractionsUtility;

    @Autowired
    public ContactInteractionService(OrganizationAccountContactRepository contactRepository, OrganizationAccountContactProfileRepository contactProfileRepository,
                                     ContactInteractionRepository contactInteractionRepository, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.contactRepository = contactRepository;
        this.membershipRetrievalService = membershipRetrievalService;
        this.contactAccessOperationUtility = new ContactAccessOperationUtility<>(membershipRetrievalService, contactProfileRepository);
        this.contactInteractionsUtility = new ContactInteractionsUtility<>(membershipRetrievalService, contactRepository, contactInteractionRepository);
    }

    public MultiInteractionModel getInteractions(UUID contactId, Integer offset, Integer limit) {
        return null;
    }

    @Transactional
    public void addInteraction(UUID contactId, ContactInteractionRequestModel requestModel) {

    }

    @Transactional
    public void updateInteraction(UUID contactId, UUID interactionId, ContactInteractionRequestModel requestModel) {

    }

}
