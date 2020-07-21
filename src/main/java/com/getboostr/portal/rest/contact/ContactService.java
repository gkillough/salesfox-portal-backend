package com.getboostr.portal.rest.contact;

import com.getboostr.portal.common.enumeration.AccessOperation;
import com.getboostr.portal.common.model.PortalAddressModel;
import com.getboostr.portal.common.service.contact.ContactAccessOperationUtility;
import com.getboostr.portal.common.service.contact.ContactFieldValidationUtils;
import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.RoleEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.account.repository.UserRepository;
import com.getboostr.portal.rest.contact.model.*;
import com.getboostr.portal.rest.security.authorization.PortalAuthorityConstants;
import com.getboostr.portal.database.organization.account.contact.Contactable;
import com.getboostr.portal.database.organization.account.contact.entity.OrganizationAccountContactAddressEntity;
import com.getboostr.portal.database.organization.account.contact.entity.OrganizationAccountContactEntity;
import com.getboostr.portal.database.organization.account.contact.entity.OrganizationAccountContactInteractionsEntity;
import com.getboostr.portal.database.organization.account.contact.entity.OrganizationAccountContactProfileEntity;
import com.getboostr.portal.database.organization.account.contact.repository.OrganizationAccountContactAddressRepository;
import com.getboostr.portal.database.organization.account.contact.repository.OrganizationAccountContactInteractionsRepository;
import com.getboostr.portal.database.organization.account.contact.repository.OrganizationAccountContactProfileRepository;
import com.getboostr.portal.database.organization.account.contact.repository.OrganizationAccountContactRepository;
import com.getboostr.portal.rest.common.model.request.ActiveStatusPatchModel;
import com.getboostr.portal.rest.common.page.PageRequestValidationUtils;
import com.getboostr.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
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

        List<OrganizationAccountContactAddressEntity> contactAddresses = contactAddressRepository.findAllByContactIdIn(contactIds);
        Map<UUID, OrganizationAccountContactAddressEntity> contactIdToAddresses = createContactableIdMap(contactAddresses);

        List<ContactResponseModel> contactModels = new ArrayList<>();
        for (OrganizationAccountContactEntity contact : accessibleContacts) {
            OrganizationAccountContactProfileEntity profile = contactIdToProfile.get(contact.getContactId());
            OrganizationAccountContactInteractionsEntity interactions = contactIdToInteractions.get(contact.getContactId());
            PointOfContactUserModel nullablePointOfContact = retrievePointOfContactIfExists(profile.getOrganizationPointOfContactUserId());

            OrganizationAccountContactAddressEntity contactAddressEntity = contactIdToAddresses.get(contact.getContactId());
            PortalAddressModel contactAddressModel = PortalAddressModel.fromEntity(contactAddressEntity);

            ContactResponseModel contactModel = new ContactResponseModel(
                    contact.getContactId(), contact.getFirstName(), contact.getLastName(), contact.getEmail(),
                    profile.getMobileNumber(), profile.getBusinessNumber(), contactAddressModel, profile.getContactOrganizationName(), profile.getTitle(),
                    interactions.getContactInitiations(), interactions.getEngagementsGenerated(), nullablePointOfContact);
            contactModels.add(contactModel);
        }

        return new MultiContactModel(contactModels, accessibleContacts);
    }

    public ContactResponseModel getContact(UUID contactId) {
        OrganizationAccountContactEntity foundContact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        boolean canUserUpdateContact = contactAccessOperationUtility.canUserAccessContact(loggedInUser, foundContact, AccessOperation.READ);
        if (!canUserUpdateContact) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Function<String, ResponseStatusException> exceptionFunction = repositoryName -> {
            log.error("Cannot find {} for contact with id [{}]", repositoryName, contactId);
            return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        };
        OrganizationAccountContactProfileEntity profile = contactProfileRepository.findByContactId(contactId)
                .orElseThrow(() -> exceptionFunction.apply("profile"));

        PointOfContactUserModel nullablePointOfContact = retrievePointOfContactIfExists(profile.getOrganizationPointOfContactUserId());
        OrganizationAccountContactInteractionsEntity interactions = contactInteractionsRepository.findById(contactId)
                .orElseThrow(() -> exceptionFunction.apply("interactions"));

        OrganizationAccountContactAddressEntity contactAddressEntity = contactAddressRepository.findByContactId(contactId)
                .orElseThrow(() -> exceptionFunction.apply("address"));
        PortalAddressModel contactAddressModel = PortalAddressModel.fromEntity(contactAddressEntity);

        return new ContactResponseModel(foundContact.getContactId(), foundContact.getFirstName(), foundContact.getLastName(), foundContact.getEmail(),
                profile.getMobileNumber(), profile.getBusinessNumber(), contactAddressModel, profile.getContactOrganizationName(), profile.getTitle(),
                interactions.getContactInitiations(), interactions.getEngagementsGenerated(), nullablePointOfContact);
    }

    @Transactional
    public void createContact(ContactUploadModel contactModel) {
        ContactFieldValidationUtils.validateContactUploadModel(contactModel);
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        RoleEntity userRole = membershipRetrievalService.getRoleEntity(userMembership);

        UUID pointOfContactUserId = contactModel.getPointOfContactUserId();
        if (isNonOrganizationRole(userRole.getRoleLevel())) {
            pointOfContactUserId = loggedInUser.getUserId();
        } else if (pointOfContactUserId != null) {
            validatePointOfContactUser(userMembership.getOrganizationAccountId(), pointOfContactUserId);
        }

        OrganizationAccountContactEntity contactToSave = new OrganizationAccountContactEntity(
                null, userMembership.getOrganizationAccountId(), contactModel.getFirstName(), contactModel.getLastName(), contactModel.getEmail(), true);
        OrganizationAccountContactEntity savedContact = contactRepository.save(contactToSave);

        OrganizationAccountContactAddressEntity contactAddressToSave = new OrganizationAccountContactAddressEntity();
        contactAddressToSave.setContactId(savedContact.getContactId());
        contactModel.getAddress().copyFieldsToEntity(contactAddressToSave);
        OrganizationAccountContactAddressEntity savedContactAddress = contactAddressRepository.save(contactAddressToSave);

        OrganizationAccountContactProfileEntity contactProfileToSave = new OrganizationAccountContactProfileEntity(
                null, savedContact.getContactId(), savedContactAddress.getAddressId(), pointOfContactUserId, contactModel.getContactOrganizationName(), contactModel.getTitle(), contactModel.getMobileNumber(), contactModel.getBusinessNumber());
        contactProfileRepository.save(contactProfileToSave);

        OrganizationAccountContactInteractionsEntity contactInteractionsToSave = new OrganizationAccountContactInteractionsEntity(savedContact.getContactId(), 0L, 0L);
        contactInteractionsRepository.save(contactInteractionsToSave);
    }

    @Transactional
    public void updateContact(UUID contactId, ContactUploadModel contactUpdateModel) {
        OrganizationAccountContactEntity contactToUpdate = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        boolean canUserUpdateContact = contactAccessOperationUtility.canUserAccessContact(loggedInUser, contactToUpdate, AccessOperation.UPDATE);
        if (!canUserUpdateContact) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        ContactFieldValidationUtils.validateContactUploadModel(contactUpdateModel);

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
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
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
        return roleLevel.equals(PortalAuthorityConstants.PORTAL_BASIC_USER) || roleLevel.equals(PortalAuthorityConstants.PORTAL_PREMIUM_USER);
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
