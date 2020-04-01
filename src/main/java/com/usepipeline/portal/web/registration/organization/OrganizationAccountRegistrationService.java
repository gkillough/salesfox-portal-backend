package com.usepipeline.portal.web.registration.organization;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

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

    public ValidationModel isAccountOwnerEmailValid(Object model) {
        // TODO implement
        return new ValidationModel(false, "A user with that email already exists");
    }

    public ValidationModel isAccountNameValid(Object model) {
        // TODO implement
        return new ValidationModel(false, "An organization account with that name exists");
    }

    @Transactional
    public void registerOrganizationAccount(OrganizationAccountRegistrationModel registrationModel) {
        LicenseEntity orgAccountLicense = getAndValidateLicenseByHash(registrationModel.getLicenseHash());
        validateRegistrationFields(registrationModel);

        OrganizationEntity orgEntity = getOrCreateOrganizationWithName(registrationModel.getOrganizationName());

        // TODO abstract the code below into individual methods

        OrganizationAccountEntity orgAccountToSave = new OrganizationAccountEntity(
                null, registrationModel.getOrganizationAccountName(), orgAccountLicense.getLicenseId(), orgEntity.getOrganizationId(), true);
        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.save(orgAccountToSave);

        PortalAddressModel addressModel = registrationModel.getOrganizationAddress();
        OrganizationAccountAddressEntity orgAccountAddressEntityToSave = new OrganizationAccountAddressEntity(null, orgAccountEntity.getOrganizationAccountId());
        addressModel.copyFieldsToEntity(orgAccountAddressEntityToSave);
        OrganizationAccountAddressEntity orgAccountAddressEntity = organizationAccountAddressRepository.save(orgAccountAddressEntityToSave);

        OrganizationAccountManagerRegistrationModel accountOwnerModel = registrationModel.getAccountOwner();
        UserRegistrationModel organizationAccountOwnerToRegister = new UserRegistrationModel(
                accountOwnerModel.getFirstName(), accountOwnerModel.getLastName(), accountOwnerModel.getEmail(), accountOwnerModel.getPassword(), "Pipeline Business");
        Long registeredUserId = userRegistrationService.registerUser(organizationAccountOwnerToRegister, true);

        UserProfileUpdateModel accountManagerProfileUpdateModel = new UserProfileUpdateModel(
                accountOwnerModel.getFirstName(), accountOwnerModel.getLastName(), accountOwnerModel.getEmail(), accountOwnerModel.getUserAddress(), accountOwnerModel.getMobilePhoneNumber(), accountOwnerModel.getBusinessPhoneNumber());
        userProfileService.updateProfile(registeredUserId, accountManagerProfileUpdateModel);

        OrganizationAccountProfileEntity orgAccountProfileToSave = new OrganizationAccountProfileEntity(
                null, orgAccountEntity.getOrganizationAccountId(), orgAccountAddressEntity.getOrganizationAccountAddressId(), registrationModel.getBusinessPhoneNumber());
        organizationAccountProfileRepository.save(orgAccountProfileToSave);
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

    private void validateRegistrationFields(OrganizationAccountRegistrationModel organizationAccountRegistrationModel) {
        // TODO implement
    }

}
