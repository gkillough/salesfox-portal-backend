package com.usepipeline.portal.web.registration.organization;

import com.usepipeline.portal.common.FieldValidationUtils;
import com.usepipeline.portal.common.model.PortalAddressModel;
import com.usepipeline.portal.database.account.entity.*;
import com.usepipeline.portal.database.account.repository.*;
import com.usepipeline.portal.web.common.model.ValidationModel;
import com.usepipeline.portal.web.registration.organization.model.OrganizationAccountManagerRegistrationModel;
import com.usepipeline.portal.web.registration.organization.model.OrganizationAccountRegistrationModel;
import com.usepipeline.portal.web.registration.user.UserRegistrationModel;
import com.usepipeline.portal.web.registration.user.UserRegistrationService;
import com.usepipeline.portal.web.user.profile.UserProfileService;
import com.usepipeline.portal.web.user.profile.model.UserProfileUpdateModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.*;

@Component
public class OrganizationAccountRegistrationService {
    private LicenseRepository licenseRepository;
    private OrganizationRepository organizationRepository;
    private OrganizationAccountRepository organizationAccountRepository;
    private OrganizationAccountAddressRepository organizationAccountAddressRepository;
    private OrganizationAccountProfileRepository organizationAccountProfileRepository;
    private UserRegistrationService userRegistrationService;
    private UserProfileService userProfileService;

    @Autowired
    public OrganizationAccountRegistrationService(LicenseRepository licenseRepository,
                                                  OrganizationRepository organizationRepository,
                                                  OrganizationAccountRepository organizationAccountRepository,
                                                  OrganizationAccountAddressRepository organizationAccountAddressRepository,
                                                  OrganizationAccountProfileRepository organizationAccountProfileRepository,
                                                  UserRegistrationService userRegistrationService, UserProfileService userProfileService) {
        this.licenseRepository = licenseRepository;
        this.organizationRepository = organizationRepository;
        this.organizationAccountRepository = organizationAccountRepository;
        this.organizationAccountAddressRepository = organizationAccountAddressRepository;
        this.organizationAccountProfileRepository = organizationAccountProfileRepository;
        this.userRegistrationService = userRegistrationService;
        this.userProfileService = userProfileService;
    }

    // TODO implement
    public ValidationModel isAccountManagerEmailValid(Object model) {
        if (userProfileService.isEmailAlreadyInUse("TODO")) {
            return ValidationModel.invalid("A user with that email already exists");
        }
        return ValidationModel.valid();
    }

    // TODO implement
    public ValidationModel isAccountNameValid(Object model) {

        if (isOrganizationAccountNameInUse("TODO orgName", "TODO")) {
            return ValidationModel.invalid("An organization account with that name exists");
        }
        return ValidationModel.valid();
    }

    @Transactional
    public void registerOrganizationAccount(OrganizationAccountRegistrationModel registrationModel) {
        LicenseEntity orgAccountLicense = getAndValidateLicenseByHash(registrationModel.getLicenseHash());
        validateRegistrationFields(registrationModel);

        OrganizationEntity orgEntity = getOrCreateOrganizationWithName(registrationModel.getOrganizationName());
        OrganizationAccountEntity orgAccountEntity = createOrganizationAccount(registrationModel, orgAccountLicense, orgEntity);
        OrganizationAccountAddressEntity orgAccountAddressEntity = createOrganizationAccountAddress(registrationModel.getOrganizationAddress(), orgAccountEntity);

        registerOrganizationAccountManager(registrationModel.getAccountManager(), orgAccountEntity);

        createOrganizationAccountProfile(
                orgAccountEntity, orgAccountAddressEntity, registrationModel.getBusinessPhoneNumber());
    }

    private boolean isOrganizationAccountNameInUse(String organizationName, String organizationAccountName) {
        return organizationRepository.findFirstByOrganizationName(organizationName)
                .map(OrganizationEntity::getOrganizationId)
                .filter(orgId -> organizationAccountRepository.findFirstByOrganizationIdAndOrganizationAccountName(orgId, organizationAccountName).isPresent())
                .isPresent();
    }

    private LicenseEntity getAndValidateLicenseByHash(UUID licenseHash) {
        if (licenseHash != null) {
            Optional<LicenseEntity> existingLicense = licenseRepository.findFirstByLicenseHash(licenseHash);
            if (existingLicense.isPresent()) {
                LicenseEntity licenseEntity = existingLicense.get();
                boolean licenseInUse = organizationAccountRepository.findFirstByLicenseId(licenseEntity.getLicenseId()).isPresent();
                if (!licenseInUse) {
                    return licenseEntity;
                }
            }
        }
        throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Invalid license");
    }

    private OrganizationEntity getOrCreateOrganizationWithName(String organizationName) {
        Optional<OrganizationEntity> optionalExistingOrganization = organizationRepository.findFirstByOrganizationName(organizationName);
        return optionalExistingOrganization.orElseGet(() -> new OrganizationEntity(null, organizationName, true));
    }

    private OrganizationAccountEntity createOrganizationAccount(OrganizationAccountRegistrationModel registrationModel, LicenseEntity license, OrganizationEntity organization) {
        OrganizationAccountEntity orgAccountToSave = new OrganizationAccountEntity(
                null, registrationModel.getOrganizationAccountName(), license.getLicenseId(), organization.getOrganizationId(), true);
        return organizationAccountRepository.save(orgAccountToSave);
    }

    private OrganizationAccountAddressEntity createOrganizationAccountAddress(PortalAddressModel addressModel, OrganizationAccountEntity organizationAccount) {
        OrganizationAccountAddressEntity orgAccountAddressEntityToSave = new OrganizationAccountAddressEntity(
                null, organizationAccount.getOrganizationAccountId());
        addressModel.copyFieldsToEntity(orgAccountAddressEntityToSave);
        return organizationAccountAddressRepository.save(orgAccountAddressEntityToSave);
    }

    private void registerOrganizationAccountManager(OrganizationAccountManagerRegistrationModel accountManagerModel, OrganizationAccountEntity organizationAccount) {
        UserRegistrationModel organizationAccountManagerToRegister = new UserRegistrationModel(
                accountManagerModel.getFirstName(), accountManagerModel.getLastName(), accountManagerModel.getEmail(), accountManagerModel.getPassword(), "Pipeline Business");
        Long registeredUserId = userRegistrationService.registerUser(organizationAccountManagerToRegister, organizationAccount.getOrganizationAccountId());

        UserProfileUpdateModel accountManagerProfileUpdateModel = new UserProfileUpdateModel(
                accountManagerModel.getFirstName(), accountManagerModel.getLastName(), accountManagerModel.getEmail(),
                accountManagerModel.getUserAddress(), accountManagerModel.getMobilePhoneNumber(), accountManagerModel.getBusinessPhoneNumber());
        userProfileService.updateProfileWithoutPermissionsCheck(registeredUserId, accountManagerProfileUpdateModel);
    }

    private OrganizationAccountProfileEntity createOrganizationAccountProfile(OrganizationAccountEntity organizationAccount, OrganizationAccountAddressEntity organizationAccountAddress, String businessPhoneNumber) {
        OrganizationAccountProfileEntity orgAccountProfileToSave = new OrganizationAccountProfileEntity(
                null, organizationAccount.getOrganizationAccountId(), organizationAccountAddress.getOrganizationAccountAddressId(), businessPhoneNumber);
        return organizationAccountProfileRepository.save(orgAccountProfileToSave);
    }

    private void validateRegistrationFields(OrganizationAccountRegistrationModel registrationModel) {
        Set<String> errorFields = new LinkedHashSet<>();
        isBlankAddError(errorFields, "Organization Name", registrationModel.getOrganizationName());
        isBlankAddError(errorFields, "Organization Account Name", registrationModel.getOrganizationAccountName());
        isBlankAddError(errorFields, "Business Phone Number", registrationModel.getBusinessPhoneNumber());

        if (isOrganizationAccountNameInUse(registrationModel.getOrganizationName(), registrationModel.getOrganizationAccountName())) {
            errorFields.add("Organization Account Name is already in use");
        }

        if (!FieldValidationUtils.isValidUSPhoneNumber(registrationModel.getBusinessPhoneNumber(), false)) {
            errorFields.add("Organization Account Phone Number is in an invalid format");
        }

        if (!FieldValidationUtils.isValidUSAddress(registrationModel.getOrganizationAddress(), true)) {
            errorFields.add("Organization Account Address is invalid");
        }

        validateOrganizationAccountManager(errorFields, registrationModel.getAccountManager());

        if (userProfileService.isEmailAlreadyInUse(registrationModel.getAccountManager().getEmail())) {
            errorFields.add("Account Manager Email is already in use");
        }

        if (!errorFields.isEmpty()) {
            String errorFieldsString = String.join(", ", errorFields);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("There are errors with the fields: %s", errorFieldsString));
        }
    }

    private void validateOrganizationAccountManager(Collection<String> errorFields, OrganizationAccountManagerRegistrationModel accountManager) {
        if (!FieldValidationUtils.isValidEmailAddress(accountManager.getEmail(), false)) {
            errorFields.add("Account Manager Email is invalid");
        } else if (!FieldValidationUtils.isValidUSPhoneNumber(accountManager.getMobilePhoneNumber(), true)) {
            errorFields.add("Account Manager Mobile Phone Number is invalid");
        } else if (!FieldValidationUtils.isValidUSPhoneNumber(accountManager.getBusinessPhoneNumber(), true)) {
            errorFields.add("Account Manager Business Phone Number is invalid");
        } else if (!FieldValidationUtils.isValidUSAddress(accountManager.getUserAddress(), true)) {
            errorFields.add("Account Manager Address is invalid");
        }
    }

    private void isBlankAddError(Collection<String> errorFields, String fieldName, String fieldValue) {
        if (StringUtils.isBlank(fieldValue)) {
            errorFields.add(String.format("%s is blank", fieldName));
        }
    }

}
