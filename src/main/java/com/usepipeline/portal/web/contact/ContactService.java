package com.usepipeline.portal.web.contact;

import com.usepipeline.portal.common.FieldValidationUtils;
import com.usepipeline.portal.common.enumeration.AccessOperation;
import com.usepipeline.portal.common.service.contact.ContactAccessOperationUtility;
import com.usepipeline.portal.database.account.entity.MembershipEntity;
import com.usepipeline.portal.database.account.entity.RoleEntity;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.account.repository.UserRepository;
import com.usepipeline.portal.database.organization.account.contact.Contactable;
import com.usepipeline.portal.database.organization.account.contact.entity.OrganizationAccountContactAddressEntity;
import com.usepipeline.portal.database.organization.account.contact.entity.OrganizationAccountContactEntity;
import com.usepipeline.portal.database.organization.account.contact.entity.OrganizationAccountContactInteractionsEntity;
import com.usepipeline.portal.database.organization.account.contact.entity.OrganizationAccountContactProfileEntity;
import com.usepipeline.portal.database.organization.account.contact.repository.OrganizationAccountContactAddressRepository;
import com.usepipeline.portal.database.organization.account.contact.repository.OrganizationAccountContactInteractionsRepository;
import com.usepipeline.portal.database.organization.account.contact.repository.OrganizationAccountContactProfileRepository;
import com.usepipeline.portal.database.organization.account.contact.repository.OrganizationAccountContactRepository;
import com.usepipeline.portal.web.common.model.request.ActiveStatusPatchModel;
import com.usepipeline.portal.web.common.page.PageRequestValidationUtils;
import com.usepipeline.portal.web.contact.model.*;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import com.usepipeline.portal.web.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ContactService {
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private UserRepository userRepository;
    private OrganizationAccountContactRepository contactRepository;
    private OrganizationAccountContactAddressRepository contactAddressRepository;
    private OrganizationAccountContactProfileRepository contactProfileRepository;
    private OrganizationAccountContactInteractionsRepository contactInteractionsRepository;
    private ContactAccessOperationUtility<ResponseStatusException> contactAccessOperationUtility;

    @Autowired
    public ContactService(HttpSafeUserMembershipRetrievalService membershipRetrievalService, UserRepository userRepository, OrganizationAccountContactRepository contactRepository,
                          OrganizationAccountContactAddressRepository contactAddressRepository, OrganizationAccountContactProfileRepository contactProfileRepository, OrganizationAccountContactInteractionsRepository contactInteractionsRepository) {
        this.membershipRetrievalService = membershipRetrievalService;
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
        this.contactAddressRepository = contactAddressRepository;
        this.contactProfileRepository = contactProfileRepository;
        this.contactInteractionsRepository = contactInteractionsRepository;
        this.contactAccessOperationUtility = new ContactAccessOperationUtility<>(membershipRetrievalService, contactProfileRepository);
    }

    public MultiContactModel getContacts(boolean contactActiveStatus, Integer pageOffset, Integer pageLimit) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);

        Page<OrganizationAccountContactEntity> accessibleContacts = getAccessibleContacts(loggedInUser, userMembership, contactActiveStatus, pageOffset, pageLimit);
        if (accessibleContacts.isEmpty()) {
            return MultiContactModel.empty();
        }

        Set<UUID> contactIds = accessibleContacts
                .stream()
                .map(OrganizationAccountContactEntity::getContactId)
                .collect(Collectors.toSet());

        List<OrganizationAccountContactProfileEntity> contactProfiles = contactProfileRepository.findAllByContactIdIn(contactIds);
        Map<UUID, OrganizationAccountContactProfileEntity> contactIdToProfile = createContactableIdMap(contactProfiles);

        List<OrganizationAccountContactInteractionsEntity> contactInteractions = contactInteractionsRepository.findAllById(contactIds);
        Map<UUID, OrganizationAccountContactInteractionsEntity> contactIdToInteractions = createContactableIdMap(contactInteractions);

        List<ContactModel> contactModels = new ArrayList<>();
        for (OrganizationAccountContactEntity contact : accessibleContacts) {
            OrganizationAccountContactProfileEntity profile = contactIdToProfile.get(contact.getContactId());
            OrganizationAccountContactInteractionsEntity interactions = contactIdToInteractions.get(contact.getContactId());
            PointOfContactUserModel nullablePointOfContact = retrievePointOfContactIfExists(profile.getOrganizationPointOfContactUserId());

            ContactModel contactModel = new ContactModel(
                    contact.getContactId(), contact.getFirstName(), contact.getLastName(), profile.getContactOrganizationName(), profile.getTitle(), interactions.getContactInitiations(), interactions.getEngagementsGenerated(), nullablePointOfContact);
            contactModels.add(contactModel);
        }

        return new MultiContactModel(contactModels, accessibleContacts);
    }

    @Transactional
    public void createContact(ContactUpdateModel contactUpdateModel) {
        validateContactUpdateModel(contactUpdateModel);
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        RoleEntity userRole = membershipRetrievalService.getRoleEntity(userMembership);

        UUID pointOfContactUserId = contactUpdateModel.getPointOfContactUserId();
        if (isNonOrganizationRole(userRole.getRoleLevel())) {
            pointOfContactUserId = loggedInUser.getUserId();
        } else if (pointOfContactUserId != null) {
            validatePointOfContactUser(userMembership.getOrganizationAccountId(), pointOfContactUserId);
        }

        OrganizationAccountContactEntity contactToSave = new OrganizationAccountContactEntity(
                null, userMembership.getOrganizationAccountId(), contactUpdateModel.getFirstName(), contactUpdateModel.getLastName(), contactUpdateModel.getEmail(), true);
        OrganizationAccountContactEntity savedContact = contactRepository.save(contactToSave);

        OrganizationAccountContactAddressEntity contactAddressToSave = new OrganizationAccountContactAddressEntity();
        contactAddressToSave.setContactId(savedContact.getContactId());
        contactUpdateModel.getAddress().copyFieldsToEntity(contactAddressToSave);
        OrganizationAccountContactAddressEntity savedContactAddress = contactAddressRepository.save(contactAddressToSave);

        OrganizationAccountContactProfileEntity contactProfileToSave = new OrganizationAccountContactProfileEntity(
                null, savedContact.getContactId(), savedContactAddress.getAddressId(), pointOfContactUserId, contactUpdateModel.getContactOrganizationName(), contactUpdateModel.getTitle(), contactUpdateModel.getMobileNumber(), contactUpdateModel.getBusinessNumber());
        contactProfileRepository.save(contactProfileToSave);

        OrganizationAccountContactInteractionsEntity contactInteractionsToSave = new OrganizationAccountContactInteractionsEntity(savedContact.getContactId(), 0L, 0L);
        contactInteractionsRepository.save(contactInteractionsToSave);
    }

    @Transactional
    public void updateContact(UUID contactId, ContactUpdateModel contactUpdateModel) {
        OrganizationAccountContactEntity contactToUpdate = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        boolean canUserUpdateContact = contactAccessOperationUtility.canUserAccessContact(loggedInUser, contactToUpdate, AccessOperation.UPDATE);
        if (!canUserUpdateContact) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        validateContactUpdateModel(contactUpdateModel);

        contactToUpdate.setFirstName(contactUpdateModel.getFirstName());
        contactToUpdate.setLastName(contactUpdateModel.getLastName());
        contactToUpdate.setEmail(contactUpdateModel.getEmail());
        OrganizationAccountContactEntity updatedContact = contactRepository.save(contactToUpdate);

        OrganizationAccountContactAddressEntity contactAddressToUpdate = contactAddressRepository.findByContactId(updatedContact.getContactId())
                .orElseThrow(() -> {
                    log.error("Missing organization account contact address for contactId: [{}]", updatedContact.getContactId());
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
        contactUpdateModel.getAddress().copyFieldsToEntity(contactAddressToUpdate);
        contactAddressRepository.save(contactAddressToUpdate);

        OrganizationAccountContactProfileEntity contactProfileToUpdate = contactProfileRepository.findByContactId(updatedContact.getContactId())
                .orElseThrow(() -> {
                    log.error("Missing organization account contact profile for contactId: [{}]", updatedContact.getContactId());
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
        contactProfileToUpdate.setContactOrganizationName(contactUpdateModel.getContactOrganizationName());
        contactProfileToUpdate.setTitle(contactUpdateModel.getTitle());
        contactProfileToUpdate.setMobileNumber(contactUpdateModel.getMobileNumber());
        contactProfileToUpdate.setBusinessNumber(contactUpdateModel.getBusinessNumber());
        contactProfileRepository.save(contactProfileToUpdate);
    }

    public void assignContactToUser(UUID contactId, PointOfContactAssignmentModel pocAssignmentModel) {
        OrganizationAccountContactEntity requestedContact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        boolean canUserUpdateContact = contactAccessOperationUtility.canUserAccessContact(loggedInUser, requestedContact, AccessOperation.UPDATE);
        if (!canUserUpdateContact) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (pocAssignmentModel.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'userId' cannot be null");
        }

        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        validatePointOfContactUser(userMembership.getOrganizationAccountId(), pocAssignmentModel.getUserId());

        OrganizationAccountContactProfileEntity contactToUpdateProfile = contactProfileRepository.findByContactId(contactId)
                .orElseThrow(() -> {
                    log.error("Missing organization account contact profile for contactId: [{}]", contactId);
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });

        contactToUpdateProfile.setOrganizationPointOfContactUserId(pocAssignmentModel.getUserId());
        contactProfileRepository.save(contactToUpdateProfile);

    }

    public void setContactActiveStatus(UUID contactId, ActiveStatusPatchModel activeStatusModel) {
        OrganizationAccountContactEntity contactToUpdate = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        boolean canUserUpdateContact = contactAccessOperationUtility.canUserAccessContact(loggedInUser, contactToUpdate, AccessOperation.SET_ACTIVE_STATUS);
        if (!canUserUpdateContact) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (activeStatusModel.getActiveStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'activeStatus' cannot be null");
        }

        contactToUpdate.setIsActive(activeStatusModel.getActiveStatus());
        contactRepository.save(contactToUpdate);
    }

    private Page<OrganizationAccountContactEntity> getAccessibleContacts(UserEntity user, MembershipEntity userMembership, boolean isActive, Integer pageOffset, Integer pageLimit) {
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        if (membershipRetrievalService.isAuthenticatedUserPipelineAdmin()) {
            return contactRepository.findAllByIsActive(isActive, pageRequest);
        }

        String roleLevel = membershipRetrievalService.getRoleEntity(userMembership).getRoleLevel();
        if (roleLevel.startsWith(PortalAuthorityConstants.ORGANIZATION_ROLE_PREFIX)) {
            return contactRepository.findByOrganizationAccountIdAndIsActive(userMembership.getOrganizationAccountId(), isActive, pageRequest);
        } else if (isNonOrganizationRole(roleLevel)) {
            Set<UUID> userContactIds = contactProfileRepository.findByOrganizationPointOfContactUserId(user.getUserId())
                    .stream()
                    .map(OrganizationAccountContactProfileEntity::getContactId)
                    .collect(Collectors.toSet());
            return contactRepository.findAllByContactIdInAndIsActive(userContactIds, isActive, pageRequest);
        }
        return Page.empty();
    }

    private <T extends Contactable> Map<UUID, T> createContactableIdMap(Collection<T> contactables) {
        return contactables
                .stream()
                .collect(Collectors.toMap(Contactable::getContactId, Function.identity()));
    }

    private PointOfContactUserModel retrievePointOfContactIfExists(UUID userId) {
        if (userId != null) {
            Optional<UserEntity> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                return PointOfContactUserModel.fromUserEntity(optionalUser.get());
            } else {
                log.error("Expected to find a point of contact user with id [{}], but none exists.", userId);
            }
        }
        return null;
    }

    private boolean isNonOrganizationRole(String roleLevel) {
        return roleLevel.equals(PortalAuthorityConstants.PIPELINE_BASIC_USER) || roleLevel.equals(PortalAuthorityConstants.PIPELINE_PREMIUM_USER);
    }

    private void validateContactUpdateModel(ContactUpdateModel contactUpdateModel) {
        Set<String> errors = new LinkedHashSet<>();
        if (StringUtils.isBlank(contactUpdateModel.getFirstName())) {
            errors.add("First Name");
        }

        if (StringUtils.isBlank(contactUpdateModel.getLastName())) {
            errors.add("Last Name");
        }

        if (StringUtils.isBlank(contactUpdateModel.getEmail())) {
            errors.add("Email");
        }

        if (!errors.isEmpty()) {
            String combinedErrors = String.join(", ", errors);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The following fields cannot be blank: %s", combinedErrors));
        }

        if (!FieldValidationUtils.isValidEmailAddress(contactUpdateModel.getEmail(), false)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The email is in an invalid format");
        }

        if (!FieldValidationUtils.isValidUSAddress(contactUpdateModel.getAddress(), true)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The address is invalid");
        }

        if (!FieldValidationUtils.isValidUSPhoneNumber(contactUpdateModel.getMobileNumber(), true)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The mobile phone number is invalid");
        }

        if (!FieldValidationUtils.isValidUSPhoneNumber(contactUpdateModel.getBusinessNumber(), true)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The business phone number is invalid");
        }
    }

    private void validatePointOfContactUser(UUID requestingUserOrgAcctId, UUID pointOfContactUserId) {
        UserEntity pocUser = userRepository.findById(pointOfContactUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "The Point of Contact User specified does not exist"));
        MembershipEntity pocUserMembership = membershipRetrievalService.getMembershipEntity(pocUser);
        if (!requestingUserOrgAcctId.equals(pocUserMembership.getOrganizationAccountId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The Point of Contact User specified cannot be assigned to this Organization Account");
        }
    }

}
