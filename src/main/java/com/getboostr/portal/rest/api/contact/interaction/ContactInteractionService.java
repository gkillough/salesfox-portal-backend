package com.getboostr.portal.rest.api.contact.interaction;

import com.getboostr.portal.common.FieldValidationUtils;
import com.getboostr.portal.common.enumeration.AccessOperation;
import com.getboostr.portal.common.enumeration.InteractionClassification;
import com.getboostr.portal.common.enumeration.InteractionMedium;
import com.getboostr.portal.common.service.contact.ContactAccessOperationUtility;
import com.getboostr.portal.common.service.contact.ContactInteractionsUtility;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.contact.OrganizationAccountContactEntity;
import com.getboostr.portal.database.contact.OrganizationAccountContactRepository;
import com.getboostr.portal.database.contact.interaction.ContactInteractionPK;
import com.getboostr.portal.database.contact.interaction.ContactInteractionRepository;
import com.getboostr.portal.database.contact.profile.OrganizationAccountContactProfileRepository;
import com.getboostr.portal.database.interactions.InteractionEntity;
import com.getboostr.portal.database.interactions.InteractionsRepository;
import com.getboostr.portal.rest.api.contact.interaction.model.ContactInteractionRequestModel;
import com.getboostr.portal.rest.api.contact.interaction.model.MultiInteractionModel;
import com.getboostr.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class ContactInteractionService {
    private final OrganizationAccountContactRepository contactRepository;
    private final ContactInteractionRepository contactInteractionRepository;
    private final InteractionsRepository interactionsRepository;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private final ContactAccessOperationUtility<ResponseStatusException> contactAccessOperationUtility;
    private final ContactInteractionsUtility<ResponseStatusException> contactInteractionsUtility;

    @Autowired
    public ContactInteractionService(OrganizationAccountContactRepository contactRepository, OrganizationAccountContactProfileRepository contactProfileRepository,
                                     ContactInteractionRepository contactInteractionRepository, InteractionsRepository interactionsRepository, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.contactRepository = contactRepository;
        this.contactInteractionRepository = contactInteractionRepository;
        this.interactionsRepository = interactionsRepository;
        this.membershipRetrievalService = membershipRetrievalService;
        this.contactAccessOperationUtility = new ContactAccessOperationUtility<>(membershipRetrievalService, contactProfileRepository);
        this.contactInteractionsUtility = new ContactInteractionsUtility<>(membershipRetrievalService, contactRepository, contactInteractionRepository);
    }

    // TODO consider adding interacting user

    public MultiInteractionModel getInteractions(UUID contactId, Integer offset, Integer limit) {
        return null;
    }

    @Transactional
    public void addInteraction(UUID contactId, ContactInteractionRequestModel requestModel) {
        OrganizationAccountContactEntity foundContact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        contactAccessOperationUtility.canUserAccessContact(loggedInUser, foundContact, AccessOperation.INTERACT);
        validateRequestModel(requestModel);

        InteractionMedium interactionMedium = InteractionMedium.valueOf(requestModel.getMedium());
        InteractionClassification interactionClassification = InteractionClassification.valueOf(requestModel.getClassification());
        contactInteractionsUtility.addContactInteraction(loggedInUser, foundContact, interactionMedium, interactionClassification, requestModel.getNote(), requestModel.getDate().toLocalDate());
    }

    @Transactional
    public void updateInteraction(UUID contactId, UUID interactionId, ContactInteractionRequestModel requestModel) {
        OrganizationAccountContactEntity foundContact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        contactAccessOperationUtility.canUserAccessContact(loggedInUser, foundContact, AccessOperation.INTERACT);
        ContactInteractionPK contactInteractionPK = new ContactInteractionPK(contactId, interactionId);
        if (!contactInteractionRepository.existsById(contactInteractionPK)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        InteractionEntity foundInteraction = interactionsRepository.findById(interactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        validateRequestModel(requestModel);

        // FIXME implement
    }

    private void validateRequestModel(ContactInteractionRequestModel requestModel) {
        List<String> errors = new ArrayList<>();
        if (StringUtils.isBlank(requestModel.getMedium())) {
            errors.add("The field 'Medium' is required");
        } else if (!EnumUtils.isValidEnum(InteractionMedium.class, requestModel.getMedium())) {
            errors.add(String.format("The Medium '%s' is invalid. Valid values: %s", requestModel.getMedium(), Arrays.toString(InteractionMedium.values())));
        }

        if (StringUtils.isBlank(requestModel.getClassification())) {
            errors.add("The field 'Classification' is required");
        } else if (!EnumUtils.isValidEnum(InteractionClassification.class, requestModel.getMedium())) {
            errors.add(String.format("The Classification '%s' is invalid. Valid values: %s", requestModel.getMedium(), Arrays.toString(InteractionClassification.values())));
        }

        if (null != requestModel.getDate() && !FieldValidationUtils.isValidDate(requestModel.getDate())) {
            errors.add("The date provided is invalid");
        }

        if (StringUtils.isBlank(requestModel.getNote())) {
            errors.add("The field 'Note' cannot be blank");
        }

        if (!errors.isEmpty()) {
            String combinedErrors = StringUtils.join(errors, ", ");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("There were errors with the request: %s", combinedErrors));
        }
    }

}
