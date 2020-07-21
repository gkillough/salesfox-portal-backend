package com.getboostr.portal.rest.contact;

import com.getboostr.portal.common.service.contact.ContactCSVFileUtils;
import com.getboostr.portal.common.service.contact.ContactFieldValidationUtils;
import com.getboostr.portal.common.service.contact.model.ContactCSVWrapper;
import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.RoleEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.organization.account.OrganizationAccountEntity;
import com.getboostr.portal.rest.contact.model.ContactBulkUploadFieldStatus;
import com.getboostr.portal.rest.contact.model.ContactBulkUploadModel;
import com.getboostr.portal.rest.contact.model.ContactBulkUploadResponse;
import com.getboostr.portal.rest.contact.model.ContactUploadModel;
import com.getboostr.portal.rest.security.authorization.PortalAuthorityConstants;
import com.getboostr.portal.database.organization.account.contact.entity.OrganizationAccountContactAddressEntity;
import com.getboostr.portal.database.organization.account.contact.entity.OrganizationAccountContactEntity;
import com.getboostr.portal.database.organization.account.contact.entity.OrganizationAccountContactInteractionsEntity;
import com.getboostr.portal.database.organization.account.contact.entity.OrganizationAccountContactProfileEntity;
import com.getboostr.portal.database.organization.account.contact.repository.OrganizationAccountContactAddressRepository;
import com.getboostr.portal.database.organization.account.contact.repository.OrganizationAccountContactInteractionsRepository;
import com.getboostr.portal.database.organization.account.contact.repository.OrganizationAccountContactProfileRepository;
import com.getboostr.portal.database.organization.account.contact.repository.OrganizationAccountContactRepository;
import com.getboostr.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@Transactional
public class ContactBulkUploadService {
    public static final String UPLOAD_INPUT_TYPE_MANUAL = "MANUAL";
    public static final String UPLOAD_INPUT_TYPE_CSV = "CSV";

    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private OrganizationAccountContactRepository contactRepository;
    private OrganizationAccountContactAddressRepository contactAddressRepository;
    private OrganizationAccountContactProfileRepository contactProfileRepository;
    private OrganizationAccountContactInteractionsRepository contactInteractionsRepository;

    @Autowired
    public ContactBulkUploadService(HttpSafeUserMembershipRetrievalService membershipRetrievalService, OrganizationAccountContactRepository contactRepository, OrganizationAccountContactAddressRepository contactAddressRepository,
                                    OrganizationAccountContactProfileRepository contactProfileRepository, OrganizationAccountContactInteractionsRepository contactInteractionsRepository) {
        this.membershipRetrievalService = membershipRetrievalService;
        this.contactRepository = contactRepository;
        this.contactAddressRepository = contactAddressRepository;
        this.contactProfileRepository = contactProfileRepository;
        this.contactInteractionsRepository = contactInteractionsRepository;
    }

    // TODO consider a response model with the upload session id
    public ContactBulkUploadResponse createContactsInBulk(ContactBulkUploadModel contactBulkUploadModel) {
        return createContactsInBulk(contactBulkUploadModel.getContacts(), UPLOAD_INPUT_TYPE_MANUAL);
    }

    private ContactBulkUploadResponse createContactsInBulk(List<ContactUploadModel> contactsUploadCandidates, String inputType) {
        if (contactsUploadCandidates == null || contactsUploadCandidates.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The no contacts were provided in the request");
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        RoleEntity userRole = membershipRetrievalService.getRoleEntity(userMembership);
        OrganizationAccountEntity userOrgAccount = membershipRetrievalService.getOrganizationAccountEntity(userMembership);

        UUID pointOfContactUserId = null;
        String userRoleLevel = userRole.getRoleLevel();
        if (PortalAuthorityConstants.PORTAL_BASIC_USER.equals(userRoleLevel) || PortalAuthorityConstants.PORTAL_PREMIUM_USER.equals(userRoleLevel)) {
            pointOfContactUserId = loggedInUser.getUserId();
        }

        List<ContactBulkUploadFieldStatus> contactUploadStatuses = new ArrayList<>(contactsUploadCandidates.size());

        List<OrganizationAccountContactEntity> contactsToSave = new ArrayList<>(contactsUploadCandidates.size());
        List<OrganizationAccountContactAddressEntity> contactAddressesToSave = new ArrayList<>(contactsUploadCandidates.size());
        List<OrganizationAccountContactProfileEntity> contactProfilesToSave = new ArrayList<>(contactsUploadCandidates.size());
        List<OrganizationAccountContactInteractionsEntity> contactInteractionsToSave = new ArrayList<>(contactsUploadCandidates.size());

        for (int i = 0; i < contactsUploadCandidates.size(); i++) {
            ContactUploadModel contactUploadCandidate = contactsUploadCandidates.get(i);
            ContactBulkUploadFieldStatus candidateStatus = createRowStatusModel(i, contactUploadCandidate);
            contactUploadStatuses.add(candidateStatus);

            if (StringUtils.isBlank(candidateStatus.getErrorMessage())) {
                UUID contactToSaveId = UUID.randomUUID();
                OrganizationAccountContactEntity contactToSave = new OrganizationAccountContactEntity(
                        contactToSaveId, userOrgAccount.getOrganizationAccountId(), contactUploadCandidate.getFirstName(), contactUploadCandidate.getLastName(), contactUploadCandidate.getEmail(), true);
                contactsToSave.add(contactToSave);

                UUID addressToSaveId = UUID.randomUUID();
                OrganizationAccountContactAddressEntity contactAddressToSave = new OrganizationAccountContactAddressEntity();
                contactAddressToSave.setAddressId(addressToSaveId);
                contactAddressToSave.setContactId(contactToSaveId);
                contactUploadCandidate.getAddress().copyFieldsToEntity(contactAddressToSave);
                contactAddressesToSave.add(contactAddressToSave);

                OrganizationAccountContactProfileEntity contactProfileToSave = new OrganizationAccountContactProfileEntity(
                        null, contactToSaveId, addressToSaveId, pointOfContactUserId, contactUploadCandidate.getContactOrganizationName(), contactUploadCandidate.getTitle(), contactUploadCandidate.getMobileNumber(), contactUploadCandidate.getBusinessNumber());
                contactProfilesToSave.add(contactProfileToSave);

                OrganizationAccountContactInteractionsEntity contactInteractionsEntityToSave = new OrganizationAccountContactInteractionsEntity(contactToSaveId, 0L, 0L);
                contactInteractionsToSave.add(contactInteractionsEntityToSave);
            }
        }

        log.info("Saving {} valid contacts out of {} to the database", contactsToSave.size(), contactsUploadCandidates.size());
        contactRepository.saveAll(contactsToSave);
        contactAddressRepository.saveAll(contactAddressesToSave);
        contactProfileRepository.saveAll(contactProfilesToSave);
        contactInteractionsRepository.saveAll(contactInteractionsToSave);

        return new ContactBulkUploadResponse(contactsUploadCandidates.size(), contactsToSave.size(), contactUploadStatuses);
    }

    // TODO consider also reading in the header mappings
    public ContactBulkUploadResponse createContactsFromCsvFile(MultipartFile csvFile) {
        if (csvFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The file cannot be empty");
        }

        Resource csvFileResource = csvFile.getResource();
        try (ContactCSVWrapper csvWrapper = ContactCSVFileUtils.createCSVWrapper(csvFileResource.getFile(), ContactCSVFileUtils.portalCSVFormat())) {
            List<ContactUploadModel> parsedContactRecords = csvWrapper.parseRecords();
            return createContactsInBulk(parsedContactRecords, UPLOAD_INPUT_TYPE_CSV);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while reading the file: " + e.getMessage());
        }
    }

    private ContactBulkUploadFieldStatus createRowStatusModel(int fieldNumber, ContactUploadModel contactUploadCandidate) {
        String errorMessage = null;
        try {
            ContactFieldValidationUtils.validateContactUploadModel(contactUploadCandidate);
        } catch (ResponseStatusException e) {
            log.warn("A contact failed validation during bulk upload: {} ", e.getMessage());
            errorMessage = e.getMessage();
        }
        return new ContactBulkUploadFieldStatus(fieldNumber, errorMessage);
    }

}
