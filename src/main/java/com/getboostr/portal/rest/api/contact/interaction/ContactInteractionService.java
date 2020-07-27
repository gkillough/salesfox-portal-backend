package com.getboostr.portal.rest.api.contact.interaction;

import com.getboostr.portal.common.FieldValidationUtils;
import com.getboostr.portal.common.enumeration.AccessOperation;
import com.getboostr.portal.common.enumeration.InteractionClassification;
import com.getboostr.portal.common.enumeration.InteractionMedium;
import com.getboostr.portal.common.service.contact.ContactAccessOperationUtility;
import com.getboostr.portal.common.service.contact.ContactInteractionsUtility;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.account.repository.UserRepository;
import com.getboostr.portal.database.contact.OrganizationAccountContactEntity;
import com.getboostr.portal.database.contact.OrganizationAccountContactRepository;
import com.getboostr.portal.database.contact.interaction.ContactInteractionEntity;
import com.getboostr.portal.database.contact.interaction.ContactInteractionRepository;
import com.getboostr.portal.database.contact.profile.OrganizationAccountContactProfileRepository;
import com.getboostr.portal.rest.api.common.page.PageRequestValidationUtils;
import com.getboostr.portal.rest.api.contact.interaction.model.ContactInteractionRequestModel;
import com.getboostr.portal.rest.api.contact.interaction.model.ContactInteractionsResponseModel;
import com.getboostr.portal.rest.api.contact.interaction.model.MultiInteractionModel;
import com.getboostr.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ContactInteractionService {
    private final OrganizationAccountContactRepository contactRepository;
    private final ContactInteractionRepository contactInteractionRepository;
    private final UserRepository userRepository;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private final ContactAccessOperationUtility<ResponseStatusException> contactAccessOperationUtility;
    private final ContactInteractionsUtility<ResponseStatusException> contactInteractionsUtility;

    @Autowired
    public ContactInteractionService(OrganizationAccountContactRepository contactRepository, OrganizationAccountContactProfileRepository contactProfileRepository,
                                     ContactInteractionRepository contactInteractionRepository, UserRepository userRepository, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.contactRepository = contactRepository;
        this.contactInteractionRepository = contactInteractionRepository;
        this.userRepository = userRepository;
        this.membershipRetrievalService = membershipRetrievalService;
        this.contactAccessOperationUtility = new ContactAccessOperationUtility<>(membershipRetrievalService, contactProfileRepository);
        this.contactInteractionsUtility = new ContactInteractionsUtility<>(membershipRetrievalService, contactRepository, contactInteractionRepository);
    }

    public MultiInteractionModel getInteractions(UUID contactId, Integer offset, Integer limit) {
        PageRequestValidationUtils.validatePagingParams(offset, limit);
        OrganizationAccountContactEntity foundContact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        if (!contactAccessOperationUtility.canUserAccessContact(loggedInUser, foundContact, AccessOperation.READ)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        PageRequest pageRequest = PageRequest.of(offset, limit);
        Page<ContactInteractionEntity> contactInteractions = contactInteractionRepository.findAllByContactId(contactId, pageRequest);
        if (contactInteractions.isEmpty()) {
            return MultiInteractionModel.empty();
        }

        List<ContactInteractionsResponseModel> responseModels = contactInteractions
                .stream()
                .map(this::convertToResponseModel)
                .collect(Collectors.toList());
        return new MultiInteractionModel(responseModels, contactInteractions);
    }

    @Transactional
    public void addInteraction(UUID contactId, ContactInteractionRequestModel requestModel) {
        OrganizationAccountContactEntity foundContact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        validateContactInteractionAccess(loggedInUser, foundContact);
        validateRequestModel(foundContact, requestModel);

        InteractionMedium interactionMedium = InteractionMedium.valueOf(requestModel.getMedium());
        InteractionClassification interactionClassification = InteractionClassification.valueOf(requestModel.getClassification());
        contactInteractionsUtility.addContactInteraction(loggedInUser, foundContact, interactionMedium, interactionClassification, requestModel.getNote(), requestModel.getDate().toLocalDate());
    }

    @Transactional
    public void updateInteraction(UUID contactId, UUID interactionId, ContactInteractionRequestModel requestModel) {
        OrganizationAccountContactEntity foundContact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        validateContactInteractionAccess(loggedInUser, foundContact);

        ContactInteractionEntity foundInteraction = contactInteractionRepository.findById(interactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        validateRequestModel(foundContact, requestModel);

        foundInteraction.setInteractingUserId(requestModel.getInteractingUserId());
        foundInteraction.setMedium(requestModel.getMedium());
        foundInteraction.setClassification(requestModel.getClassification());
        foundInteraction.setDate(requestModel.getDate().toLocalDate());
        foundInteraction.setNote(requestModel.getNote());
        contactInteractionRepository.save(foundInteraction);
    }

    private void validateContactInteractionAccess(UserEntity loggedInUser, OrganizationAccountContactEntity contact) {
        if (!contactAccessOperationUtility.canUserAccessContact(loggedInUser, contact, AccessOperation.INTERACT)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private void validateRequestModel(OrganizationAccountContactEntity contact, ContactInteractionRequestModel requestModel) {
        List<String> errors = new ArrayList<>();

        if (null != requestModel.getInteractingUserId()) {
            Optional<UserEntity> optionalInteractingUser = userRepository.findById(requestModel.getInteractingUserId());
            if (optionalInteractingUser.isPresent()) {
                if (!contactAccessOperationUtility.canUserAccessContact(optionalInteractingUser.get(), contact, AccessOperation.READ)) {
                    errors.add("The user associated with the provided interactingUserId cannot access the specified contact");
                }
            } else {
                errors.add("The interactingUserId is invalid");
            }
        }

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

    private ContactInteractionsResponseModel convertToResponseModel(ContactInteractionEntity entity) {
        return new ContactInteractionsResponseModel(entity.getInteractionId(), entity.getInteractingUserId(), entity.getMedium(), entity.getClassification(), entity.getDate(), entity.getNote());
    }

}
