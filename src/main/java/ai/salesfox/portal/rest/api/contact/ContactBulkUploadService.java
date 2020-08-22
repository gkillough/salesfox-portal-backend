package ai.salesfox.portal.rest.api.contact;

import ai.salesfox.portal.rest.api.contact.model.ContactBulkUploadFieldStatus;
import ai.salesfox.portal.rest.api.contact.model.ContactBulkUploadModel;
import ai.salesfox.portal.rest.api.contact.model.ContactBulkUploadResponse;
import ai.salesfox.portal.rest.api.contact.model.ContactUploadModel;
import ai.salesfox.portal.common.service.contact.ContactCSVFileUtils;
import ai.salesfox.portal.common.service.contact.ContactFieldValidationUtils;
import ai.salesfox.portal.common.service.contact.model.ContactCSVWrapper;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.RoleEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactRepository;
import ai.salesfox.portal.database.contact.address.OrganizationAccountContactAddressEntity;
import ai.salesfox.portal.database.contact.address.OrganizationAccountContactAddressRepository;
import ai.salesfox.portal.database.contact.profile.OrganizationAccountContactProfileEntity;
import ai.salesfox.portal.database.contact.profile.OrganizationAccountContactProfileRepository;
import ai.salesfox.portal.database.contact.restriction.ContactOrganizationAccountRestrictionEntity;
import ai.salesfox.portal.database.contact.restriction.ContactOrganizationAccountRestrictionRepository;
import ai.salesfox.portal.database.contact.restriction.ContactUserRestrictionEntity;
import ai.salesfox.portal.database.contact.restriction.ContactUserRestrictionRepository;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class ContactBulkUploadService {
    public static final String UPLOAD_INPUT_TYPE_MANUAL = "MANUAL";
    public static final String UPLOAD_INPUT_TYPE_CSV = "CSV";

    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private final OrganizationAccountContactRepository contactRepository;
    private final ContactUserRestrictionRepository contactUserRestrictionRepository;
    private final ContactOrganizationAccountRestrictionRepository contactOrgAcctRestrictionRepository;
    private final OrganizationAccountContactAddressRepository contactAddressRepository;
    private final OrganizationAccountContactProfileRepository contactProfileRepository;

    @Autowired
    public ContactBulkUploadService(HttpSafeUserMembershipRetrievalService membershipRetrievalService, OrganizationAccountContactRepository contactRepository,
                                    ContactUserRestrictionRepository contactUserRestrictionRepository, ContactOrganizationAccountRestrictionRepository contactOrgAcctRestrictionRepository,
                                    OrganizationAccountContactAddressRepository contactAddressRepository, OrganizationAccountContactProfileRepository contactProfileRepository) {
        this.membershipRetrievalService = membershipRetrievalService;
        this.contactRepository = contactRepository;
        this.contactUserRestrictionRepository = contactUserRestrictionRepository;
        this.contactOrgAcctRestrictionRepository = contactOrgAcctRestrictionRepository;
        this.contactAddressRepository = contactAddressRepository;
        this.contactProfileRepository = contactProfileRepository;
    }

    // TODO consider a response model with the upload session id
    @Transactional
    public ContactBulkUploadResponse createContactsInBulk(ContactBulkUploadModel contactBulkUploadModel) {
        return createContactsInBulk(contactBulkUploadModel.getContacts(), UPLOAD_INPUT_TYPE_MANUAL);
    }

    // TODO consider also reading in the header mappings
    @Transactional
    public ContactBulkUploadResponse createContactsFromCsvFile(MultipartFile csvFile) {
        if (csvFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The file cannot be empty");
        }

        Resource csvFileResource = csvFile.getResource();
        try (InputStream csvFileInputStream = csvFileResource.getInputStream(); ContactCSVWrapper csvWrapper = ContactCSVFileUtils.createCSVWrapper(csvFileInputStream, ContactCSVFileUtils.portalCSVFormat())) {
            List<ContactUploadModel> parsedContactRecords = csvWrapper.parseRecords();
            return createContactsInBulk(parsedContactRecords, UPLOAD_INPUT_TYPE_CSV);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while reading the file: " + e.getMessage());
        }
    }

    private ContactBulkUploadResponse createContactsInBulk(List<ContactUploadModel> contactsUploadCandidates, String inputType) {
        if (contactsUploadCandidates == null || contactsUploadCandidates.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The no contacts were provided in the request");
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        RoleEntity userRole = userMembership.getRoleEntity();

        UUID restrictedUserId = null;
        String userRoleLevel = userRole.getRoleLevel();
        if (PortalAuthorityConstants.PORTAL_BASIC_USER.equals(userRoleLevel) || PortalAuthorityConstants.PORTAL_PREMIUM_USER.equals(userRoleLevel)) {
            restrictedUserId = loggedInUser.getUserId();
        }

        List<ContactBulkUploadFieldStatus> contactUploadStatuses = new ArrayList<>(contactsUploadCandidates.size());

        List<OrganizationAccountContactEntity> contactsToSave = new ArrayList<>(contactsUploadCandidates.size());
        List<ContactOrganizationAccountRestrictionEntity> contactOrgAcctRestrictionsToSave = new ArrayList<>(contactsUploadCandidates.size());
        List<ContactUserRestrictionEntity> contactUserRestrictionsToSave = new ArrayList<>(contactsUploadCandidates.size());
        List<OrganizationAccountContactAddressEntity> contactAddressesToSave = new ArrayList<>(contactsUploadCandidates.size());
        List<OrganizationAccountContactProfileEntity> contactProfilesToSave = new ArrayList<>(contactsUploadCandidates.size());

        for (int i = 0; i < contactsUploadCandidates.size(); i++) {
            ContactUploadModel contactUploadCandidate = contactsUploadCandidates.get(i);
            ContactBulkUploadFieldStatus candidateStatus = createRowStatusModel(i, contactUploadCandidate);
            contactUploadStatuses.add(candidateStatus);

            if (StringUtils.isBlank(candidateStatus.getErrorMessage())) {
                UUID contactToSaveId = UUID.randomUUID();
                OrganizationAccountContactEntity contactToSave = new OrganizationAccountContactEntity(contactToSaveId, contactUploadCandidate.getFirstName(), contactUploadCandidate.getLastName(), contactUploadCandidate.getEmail(), true);
                contactsToSave.add(contactToSave);

                if (restrictedUserId != null) {
                    ContactUserRestrictionEntity contactUserRestrictionToSave = new ContactUserRestrictionEntity(contactToSaveId, restrictedUserId);
                    contactUserRestrictionsToSave.add(contactUserRestrictionToSave);
                } else {
                    ContactOrganizationAccountRestrictionEntity contactOrgAcctRestrictionToSave = new ContactOrganizationAccountRestrictionEntity(contactToSaveId, userMembership.getOrganizationAccountId());
                    contactOrgAcctRestrictionsToSave.add(contactOrgAcctRestrictionToSave);
                }

                OrganizationAccountContactAddressEntity contactAddressToSave = new OrganizationAccountContactAddressEntity();
                contactAddressToSave.setContactId(contactToSaveId);
                contactUploadCandidate.getAddress().copyFieldsToEntity(contactAddressToSave);
                contactAddressesToSave.add(contactAddressToSave);

                OrganizationAccountContactProfileEntity contactProfileToSave = new OrganizationAccountContactProfileEntity(
                        contactToSaveId, restrictedUserId, contactUploadCandidate.getContactOrganizationName(), contactUploadCandidate.getTitle(), contactUploadCandidate.getMobileNumber(), contactUploadCandidate.getBusinessNumber());
                contactProfilesToSave.add(contactProfileToSave);
            }
        }

        log.info("Saving {} valid contacts out of {} to the database", contactsToSave.size(), contactsUploadCandidates.size());
        contactRepository.saveAll(contactsToSave);
        contactUserRestrictionRepository.saveAll(contactUserRestrictionsToSave);
        contactOrgAcctRestrictionRepository.saveAll(contactOrgAcctRestrictionsToSave);
        contactAddressRepository.saveAll(contactAddressesToSave);
        contactProfileRepository.saveAll(contactProfilesToSave);
        return new ContactBulkUploadResponse(contactsUploadCandidates.size(), contactsToSave.size(), contactUploadStatuses);
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
