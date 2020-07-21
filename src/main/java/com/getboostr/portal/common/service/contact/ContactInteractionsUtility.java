package com.getboostr.portal.common.service.contact;

import com.getboostr.portal.common.service.auth.AbstractMembershipRetrievalService;
import com.getboostr.portal.database.organization.account.contact.entity.OrganizationAccountContactEntity;
import com.getboostr.portal.database.organization.account.contact.entity.OrganizationAccountContactInteractionsEntity;
import com.getboostr.portal.database.organization.account.contact.repository.OrganizationAccountContactInteractionsRepository;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class ContactInteractionsUtility<E extends Throwable> {
    private AbstractMembershipRetrievalService<E> membershipRetrievalService;
    private OrganizationAccountContactInteractionsRepository contactInteractionsRepository;

    public ContactInteractionsUtility(AbstractMembershipRetrievalService<E> membershipRetrievalService, OrganizationAccountContactInteractionsRepository contactInteractionsRepository) {
        this.membershipRetrievalService = membershipRetrievalService;
        this.contactInteractionsRepository = contactInteractionsRepository;
    }

    public void incrementContactInitiations(OrganizationAccountContactEntity contact) throws E {
        increment(contact, OrganizationAccountContactInteractionsEntity::getContactInitiations, OrganizationAccountContactInteractionsEntity::setContactInitiations);
    }

    public void incrementEngagementsGenerated(OrganizationAccountContactEntity contact) throws E {
        increment(contact, OrganizationAccountContactInteractionsEntity::getEngagementsGenerated, OrganizationAccountContactInteractionsEntity::setEngagementsGenerated);
    }

    private void increment(OrganizationAccountContactEntity contact, Function<OrganizationAccountContactInteractionsEntity, Long> getter, BiConsumer<OrganizationAccountContactInteractionsEntity, Long> setter) throws E {
        OrganizationAccountContactInteractionsEntity contactInteractions = contactInteractionsRepository.findById(contact.getContactId())
                .orElseThrow(membershipRetrievalService::unexpectedErrorDuringRetrieval);
        Long incrementedValue = getter.apply(contactInteractions) + 1L;
        setter.accept(contactInteractions, incrementedValue);
        contactInteractionsRepository.save(contactInteractions);
    }

}
