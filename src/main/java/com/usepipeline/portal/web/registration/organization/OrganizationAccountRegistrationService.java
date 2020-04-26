package com.usepipeline.portal.web.registration.organization;

import com.usepipeline.portal.common.FieldValidationUtils;
import com.usepipeline.portal.common.model.PortalAddressModel;
import com.usepipeline.portal.database.account.entity.LicenseEntity;
import com.usepipeline.portal.database.account.repository.LicenseRepository;
import com.usepipeline.portal.database.organization.OrganizationEntity;
import com.usepipeline.portal.database.organization.OrganizationRepository;
import com.usepipeline.portal.database.organization.account.OrganizationAccountEntity;
import com.usepipeline.portal.database.organization.account.OrganizationAccountRepository;
import com.usepipeline.portal.database.organization.account.address.OrganizationAccountAddressEntity;
import com.usepipeline.portal.database.organization.account.address.OrganizationAccountAddressRepository;
import com.usepipeline.portal.database.organization.account.profile.OrganizationAccountProfileEntity;
import com.usepipeline.portal.database.organization.account.profile.OrganizationAccountProfileRepository;
import com.usepipeline.portal.web.common.model.EmailToValidateModel;
import com.usepipeline.portal.web.common.model.ValidationResponseModel;
import com.usepipeline.portal.web.organization.common.OrganizationValidationService;
import com.usepipeline.portal.web.registration.organization.model.OrganizationAccountNameToValidateModel;
import com.usepipeline.portal.web.registration.organization.model.OrganizationAccountRegistrationModel;
import com.usepipeline.portal.web.registration.organization.model.OrganizationAccountUserRegistrationModel;
import com.usepipeline.portal.web.registration.user.UserRegistrationModel;
import com.usepipeline.portal.web.registration.user.UserRegistrationService;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
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
    private OrganizationValidationService organizationValidationService;
    private UserRegistrationService userRegistrationService;
    private UserProfileService userProfileService;

    @Autowired
    public OrganizationAccountRegistrationService(LicenseRepository licenseRepository,
                                                  OrganizationRepository organizationRepository,
                                                  OrganizationAccountRepository organizationAccountRepository,
                                                  OrganizationAccountAddressRepository organizationAccountAddressRepository,
                                                  OrganizationAccountProfileRepository organizationAccountProfileRepository,
                                                  OrganizationValidationService organizationValidationService, UserRegistrationService userRegistrationService, UserProfileService userProfileService) {
        this.licenseRepository = licenseRepository;
        this.organizationRepository = organizationRepository;
        this.organizationAccountRepository = organizationAccountRepository;
        this.organizationAccountAddressRepository = organizationAccountAddressRepository;
        this.organizationAccountProfileRepository = organizationAccountProfileRepository;
        this.organizationValidationService = organizationValidationService;
        this.userRegistrationService = userRegistrationService;
        this.userProfileService = userProfileService;
    }

    public ValidationResponseModel isAccountOwnerEmailValid(EmailToValidateModel model) {
        if (userProfileService.isEmailAlreadyInUse(model.getEmail())) {
            return ValidationResponseModel.invalid("That Email is already in use");
        }
        return ValidationResponseModel.valid();
    }

    public ValidationResponseModel isOrganizationAccountNameValid(OrganizationAccountNameToValidateModel model) {
        if (StringUtils.isBlank(model.getOrganizationName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The Organization Name cannot be blank");
        }

        if (StringUtils.isBlank(model.getOrganizationAccountName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The Organization Account Name cannot be blank");
        }

        if (organizationValidationService.isOrganizationRestricted(model.getOrganizationName())) {
            return ValidationResponseModel.invalid("That Organization Name is not allowed");
        }

        if (organizationValidationService.isOrganizationAccountNameInUse(model.getOrganizationName(), model.getOrganizationAccountName())) {
            return ValidationResponseModel.invalid("An Organization Account with that name exists");
        }
        return ValidationResponseModel.valid();
    }

    @Transactional
    public void registerOrganizationAccount(OrganizationAccountRegistrationModel registrationModel) {
        LicenseEntity orgAccountLicense = getAndValidateLicenseByHash(registrationModel.getLicenseHash());
        validateRegistrationFields(registrationModel);

        OrganizationEntity orgEntity = getOrCreateOrganizationWithName(registrationModel.getOrganizationName());
        OrganizationAccountEntity orgAccountEntity = createOrganizationAccount(registrationModel, orgAccountLicense, orgEntity);
        OrganizationAccountAddressEntity orgAccountAddressEntity = createOrganizationAccountAddress(registrationModel.getOrganizationAddress(), orgAccountEntity);

        activateLicense(orgAccountLicense);
        registerOrganizationAccountOwner(registrationModel.getAccountOwner(), orgEntity, orgAccountEntity);
        createOrganizationAccountProfile(orgAccountEntity, orgAccountAddressEntity, registrationModel.getBusinessPhoneNumber());
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
        if (optionalExistingOrganization.isPresent()) {
            return optionalExistingOrganization.get();
        }
        OrganizationEntity newOrganizationEntity = new OrganizationEntity(null, organizationName, true);
        return organizationRepository.save(newOrganizationEntity);
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

    private void registerOrganizationAccountOwner(OrganizationAccountUserRegistrationModel accountOwnerModel, OrganizationEntity organization, OrganizationAccountEntity organizationAccount) {
        UserRegistrationModel organizationAccountOwnerToRegister = new UserRegistrationModel(
                accountOwnerModel.getFirstName(), accountOwnerModel.getLastName(), accountOwnerModel.getEmail(), accountOwnerModel.getPassword(), organizationAccount.getOrganizationAccountName());
        Long registeredUserId = userRegistrationService.registerOrganizationUser(organizationAccountOwnerToRegister, organization.getOrganizationId(), PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER);

        UserProfileUpdateModel accountOwnerProfileUpdateModel = new UserProfileUpdateModel(
                accountOwnerModel.getFirstName(), accountOwnerModel.getLastName(), accountOwnerModel.getEmail(),
                accountOwnerModel.getUserAddress(), accountOwnerModel.getMobilePhoneNumber(), accountOwnerModel.getBusinessPhoneNumber());
        userProfileService.updateProfileWithoutPermissionsCheck(registeredUserId, accountOwnerProfileUpdateModel);
    }

    private OrganizationAccountProfileEntity createOrganizationAccountProfile(OrganizationAccountEntity organizationAccount, OrganizationAccountAddressEntity organizationAccountAddress, String businessPhoneNumber) {
        OrganizationAccountProfileEntity orgAccountProfileToSave = new OrganizationAccountProfileEntity(
                null, organizationAccount.getOrganizationAccountId(), organizationAccountAddress.getOrganizationAccountAddressId(), businessPhoneNumber);
        return organizationAccountProfileRepository.save(orgAccountProfileToSave);
    }

    private void activateLicense(LicenseEntity license) {
        license.setIsActive(true);
        licenseRepository.save(license);
    }

    private void validateRegistrationFields(OrganizationAccountRegistrationModel registrationModel) {
        Set<String> errorFields = new LinkedHashSet<>();
        isBlankAddError(errorFields, "Organization Name", registrationModel.getOrganizationName());
        isBlankAddError(errorFields, "Organization Account Name", registrationModel.getOrganizationAccountName());
        isBlankAddError(errorFields, "Business Phone Number", registrationModel.getBusinessPhoneNumber());

        if (organizationValidationService.isOrganizationRestricted(registrationModel.getOrganizationName())) {
            errorFields.add("That Organization Name is not allowed");
        }

        if (organizationValidationService.isOrganizationAccountNameInUse(registrationModel.getOrganizationName(), registrationModel.getOrganizationAccountName())) {
            errorFields.add("That Organization Account Name is already in use");
        }

        if (!FieldValidationUtils.isValidUSPhoneNumber(registrationModel.getBusinessPhoneNumber(), false)) {
            errorFields.add("That Organization Account Phone Number is in an invalid format");
        }

        if (registrationModel.getOrganizationAddress() == null) {
            errorFields.add("Missing Organization Account Address details");
        } else if (!FieldValidationUtils.isValidUSAddress(registrationModel.getOrganizationAddress(), true)) {
            errorFields.add("That Organization Account Address is invalid");
        }

        if (registrationModel.getAccountOwner() == null) {
            errorFields.add("Missing Account Owner details");
        } else {
            validateOrganizationAccountOwner(errorFields, registrationModel.getAccountOwner());
        }

        if (userProfileService.isEmailAlreadyInUse(registrationModel.getAccountOwner().getEmail())) {
            errorFields.add("That Account Owner Email is already in use");
        }

        if (!errorFields.isEmpty()) {
            String errorFieldsString = String.join(", ", errorFields);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("There are errors with the fields: %s", errorFieldsString));
        }
    }

    private void validateOrganizationAccountOwner(Collection<String> errorFields, OrganizationAccountUserRegistrationModel accountOwner) {
        if (!FieldValidationUtils.isValidEmailAddress(accountOwner.getEmail(), false)) {
            errorFields.add("Account Owner Email is invalid");
        }
        if (!FieldValidationUtils.isValidUSPhoneNumber(accountOwner.getMobilePhoneNumber(), true)) {
            errorFields.add("Account Owner Mobile Phone Number is invalid");
        }
        if (!FieldValidationUtils.isValidUSPhoneNumber(accountOwner.getBusinessPhoneNumber(), true)) {
            errorFields.add("Account Owner Business Phone Number is invalid");
        }
        if (!FieldValidationUtils.isValidUSAddress(accountOwner.getUserAddress(), true)) {
            errorFields.add("Account Owner Address is invalid");
        }
    }

    private void isBlankAddError(Collection<String> errorFields, String fieldName, String fieldValue) {
        if (StringUtils.isBlank(fieldValue)) {
            errorFields.add(String.format("%s is blank", fieldName));
        }
    }

}
