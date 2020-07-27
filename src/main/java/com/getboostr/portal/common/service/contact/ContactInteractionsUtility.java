package com.getboostr.portal.common.service.contact;

import com.getboostr.portal.common.enumeration.InteractionClassification;
import com.getboostr.portal.common.enumeration.InteractionMedium;
import com.getboostr.portal.common.service.auth.AbstractMembershipRetrievalService;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.contact.OrganizationAccountContactEntity;
import com.getboostr.portal.database.contact.OrganizationAccountContactRepository;
import com.getboostr.portal.database.contact.interaction.ContactInteractionRepository;

import java.time.LocalDate;
import java.util.UUID;

public class ContactInteractionsUtility<E extends Throwable> {
    private final AbstractMembershipRetrievalService<E> membershipRetrievalService;
    private final OrganizationAccountContactRepository contactRepository;
    private final ContactInteractionRepository contactInteractionRepository;

    public ContactInteractionsUtility(AbstractMembershipRetrievalService<E> membershipRetrievalService, OrganizationAccountContactRepository contactRepository, ContactInteractionRepository contactInteractionRepository) {
        this.membershipRetrievalService = membershipRetrievalService;
        this.contactRepository = contactRepository;
        this.contactInteractionRepository = contactInteractionRepository;
    }

    public void addContactInteraction(UserEntity interactingUser, UUID contactId, InteractionMedium medium, InteractionClassification classification, String note) throws E {
        OrganizationAccountContactEntity foundContact = contactRepository.findById(contactId)
                .orElseThrow(membershipRetrievalService::unexpectedErrorDuringRetrieval);
        addContactInteraction(interactingUser, foundContact, medium, classification, note);
    }

    public void addContactInteraction(UserEntity interactingUser, OrganizationAccountContactEntity contact, InteractionMedium medium, InteractionClassification classification, String note) {
        // FIXME implement
    }

    public void addContactInteraction(UserEntity interactingUser, OrganizationAccountContactEntity contact, InteractionMedium medium, InteractionClassification classification, String note, LocalDate date) {
        // FIXME implement
    }

}
