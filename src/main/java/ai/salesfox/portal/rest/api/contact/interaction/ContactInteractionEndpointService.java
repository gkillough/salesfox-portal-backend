package ai.salesfox.portal.rest.api.contact.interaction;

import ai.salesfox.portal.common.FieldValidationUtils;
import ai.salesfox.portal.common.enumeration.AccessOperation;
import ai.salesfox.portal.common.enumeration.InteractionClassification;
import ai.salesfox.portal.common.enumeration.InteractionMedium;
import ai.salesfox.portal.common.service.contact.ContactAccessOperationUtility;
import ai.salesfox.portal.common.service.contact.ContactInteractionsService;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.account.repository.UserRepository;
import ai.salesfox.portal.database.contact.OrganizationAccountContactEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactRepository;
import ai.salesfox.portal.database.contact.interaction.ContactInteractionEntity;
import ai.salesfox.portal.database.contact.interaction.ContactInteractionRepository;
import ai.salesfox.portal.rest.api.common.page.PageRequestValidationUtils;
import ai.salesfox.portal.rest.api.contact.interaction.model.ContactInteractionRequestModel;
import ai.salesfox.portal.rest.api.contact.interaction.model.ContactInteractionsResponseModel;
import ai.salesfox.portal.rest.api.contact.interaction.model.MultiInteractionModel;
import ai.salesfox.portal.rest.api.user.common.model.UserSummaryModel;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ContactInteractionEndpointService {
    private final OrganizationAccountContactRepository contactRepository;
    private final ContactInteractionRepository contactInteractionRepository;
    private final ContactInteractionsService contactInteractionsService;

    private final UserRepository userRepository;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private final ContactAccessOperationUtility contactAccessOperationUtility;

    @Autowired
    public ContactInteractionEndpointService(OrganizationAccountContactRepository contactRepository, ContactInteractionRepository contactInteractionRepository,
                                             ContactInteractionsService contactInteractionsService, UserRepository userRepository, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.contactRepository = contactRepository;
        this.contactInteractionRepository = contactInteractionRepository;
        this.contactInteractionsService = contactInteractionsService;
        this.userRepository = userRepository;
        this.membershipRetrievalService = membershipRetrievalService;
        this.contactAccessOperationUtility = new ContactAccessOperationUtility(contactRepository);
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
        if (!contactAccessOperationUtility.canUserAccessContact(loggedInUser, foundContact, AccessOperation.INTERACT)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        UserEntity interactingUser = Optional.ofNullable(requestModel.getInteractingUserId())
                .flatMap(userRepository::findById)
                .orElse(loggedInUser);
        validateRequestModel(foundContact, interactingUser, requestModel);

        InteractionMedium interactionMedium = InteractionMedium.valueOf(requestModel.getMedium());
        InteractionClassification interactionClassification = InteractionClassification.valueOf(requestModel.getClassification());
        contactInteractionsService.addContactInteraction(interactingUser, foundContact, interactionMedium, interactionClassification, requestModel.getNote(), requestModel.getDate().toLocalDate());
    }

    @Transactional
    public void updateInteraction(UUID contactId, UUID interactionId, ContactInteractionRequestModel requestModel) {
        OrganizationAccountContactEntity foundContact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        if (!contactAccessOperationUtility.canUserAccessContact(loggedInUser, foundContact, AccessOperation.INTERACT)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        ContactInteractionEntity foundInteraction = contactInteractionRepository.findById(interactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity interactingUser = Optional.ofNullable(requestModel.getInteractingUserId())
                .flatMap(userRepository::findById)
                .orElse(loggedInUser);
        validateRequestModel(foundContact, interactingUser, requestModel);

        foundInteraction.setInteractingUserId(interactingUser.getUserId());
        foundInteraction.setMedium(requestModel.getMedium());
        foundInteraction.setClassification(requestModel.getClassification());
        foundInteraction.setDate(requestModel.getDate().toLocalDate());
        foundInteraction.setNote(requestModel.getNote());
        contactInteractionRepository.save(foundInteraction);
    }

    private void validateRequestModel(OrganizationAccountContactEntity contact, @Nullable UserEntity interactingUser, ContactInteractionRequestModel requestModel) {
        List<String> errors = new ArrayList<>();

        if (null != interactingUser) {
            if (!contactAccessOperationUtility.canUserAccessContact(interactingUser, contact, AccessOperation.READ)) {
                errors.add("The user associated with the provided interactingUserId (or the current user if no interacting user was provided) cannot access the specified contact");
            }
        } else {
            errors.add("The interactingUserId is invalid");
        }

        if (StringUtils.isBlank(requestModel.getMedium())) {
            errors.add("The field 'Medium' is required");
        } else if (!EnumUtils.isValidEnum(InteractionMedium.class, requestModel.getMedium())) {
            errors.add(String.format("The Medium '%s' is invalid. Valid values: %s", requestModel.getMedium(), Arrays.toString(InteractionMedium.values())));
        }

        if (StringUtils.isBlank(requestModel.getClassification())) {
            errors.add("The field 'Classification' is required");
        } else if (!EnumUtils.isValidEnum(InteractionClassification.class, requestModel.getClassification())) {
            errors.add(String.format("The Classification '%s' is invalid. Valid values: %s", requestModel.getClassification(), Arrays.toString(InteractionClassification.values())));
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
        UserEntity interactingUserEntity = entity.getUserEntity();
        UserSummaryModel interactingUserModel = UserSummaryModel.fromEntity(interactingUserEntity);
        return new ContactInteractionsResponseModel(entity.getInteractionId(), interactingUserModel, entity.getMedium(), entity.getClassification(), entity.getDate(), entity.getNote());
    }

}
