package ai.salesfox.portal.rest.api.registration.organization;

import ai.salesfox.portal.common.FieldValidationUtils;
import ai.salesfox.portal.common.model.PortalAddressModel;
import ai.salesfox.portal.database.account.entity.LicenseEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.account.repository.LicenseRepository;
import ai.salesfox.portal.database.inventory.InventoryEntity;
import ai.salesfox.portal.database.inventory.InventoryRepository;
import ai.salesfox.portal.database.inventory.restriction.InventoryOrganizationAccountRestrictionEntity;
import ai.salesfox.portal.database.inventory.restriction.InventoryOrganizationAccountRestrictionRepository;
import ai.salesfox.portal.database.note.credit.NoteCreditEntity;
import ai.salesfox.portal.database.note.credit.NoteCreditOrgAccountRestrictionEntity;
import ai.salesfox.portal.database.note.credit.NoteCreditOrgAccountRestrictionRepository;
import ai.salesfox.portal.database.note.credit.NoteCreditRepository;
import ai.salesfox.portal.database.organization.OrganizationEntity;
import ai.salesfox.portal.database.organization.OrganizationRepository;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountRepository;
import ai.salesfox.portal.database.organization.account.address.OrganizationAccountAddressEntity;
import ai.salesfox.portal.database.organization.account.address.OrganizationAccountAddressRepository;
import ai.salesfox.portal.database.organization.account.profile.OrganizationAccountProfileEntity;
import ai.salesfox.portal.database.organization.account.profile.OrganizationAccountProfileRepository;
import ai.salesfox.portal.rest.api.common.model.request.EmailToValidateModel;
import ai.salesfox.portal.rest.api.common.model.response.ValidationResponseModel;
import ai.salesfox.portal.rest.api.organization.common.OrganizationValidationService;
import ai.salesfox.portal.rest.api.registration.organization.model.OrganizationAccountNameToValidateModel;
import ai.salesfox.portal.rest.api.registration.organization.model.OrganizationAccountRegistrationModel;
import ai.salesfox.portal.rest.api.registration.organization.model.OrganizationAccountUserRegistrationModel;
import ai.salesfox.portal.rest.api.registration.user.UserRegistrationModel;
import ai.salesfox.portal.rest.api.registration.user.UserRegistrationService;
import ai.salesfox.portal.rest.api.user.profile.UserProfileService;
import ai.salesfox.portal.rest.api.user.profile.model.UserProfileUpdateModel;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.*;

@Component
public class OrganizationAccountRegistrationService {
    private final LicenseRepository licenseRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationAccountRepository organizationAccountRepository;
    private final OrganizationAccountAddressRepository organizationAccountAddressRepository;
    private final OrganizationAccountProfileRepository organizationAccountProfileRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryOrganizationAccountRestrictionRepository inventoryOrgAcctRestrictionRepository;
    private final NoteCreditRepository noteCreditRepository;
    private final NoteCreditOrgAccountRestrictionRepository noteCreditOrgAccountRestrictionRepository;
    private final OrganizationValidationService organizationValidationService;
    private final UserRegistrationService userRegistrationService;
    private final UserProfileService userProfileService;

    @Autowired
    public OrganizationAccountRegistrationService(LicenseRepository licenseRepository,
                                                  OrganizationRepository organizationRepository,
                                                  OrganizationAccountRepository organizationAccountRepository,
                                                  OrganizationAccountAddressRepository organizationAccountAddressRepository,
                                                  OrganizationAccountProfileRepository organizationAccountProfileRepository,
                                                  InventoryRepository inventoryRepository,
                                                  InventoryOrganizationAccountRestrictionRepository inventoryOrgAcctRestrictionRepository,
                                                  NoteCreditRepository noteCreditRepository,
                                                  NoteCreditOrgAccountRestrictionRepository noteCreditOrgAccountRestrictionRepository,
                                                  OrganizationValidationService organizationValidationService,
                                                  UserRegistrationService userRegistrationService,
                                                  UserProfileService userProfileService
    ) {
        this.licenseRepository = licenseRepository;
        this.organizationRepository = organizationRepository;
        this.organizationAccountRepository = organizationAccountRepository;
        this.organizationAccountAddressRepository = organizationAccountAddressRepository;
        this.organizationAccountProfileRepository = organizationAccountProfileRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryOrgAcctRestrictionRepository = inventoryOrgAcctRestrictionRepository;
        this.noteCreditRepository = noteCreditRepository;
        this.noteCreditOrgAccountRestrictionRepository = noteCreditOrgAccountRestrictionRepository;
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
        createOrganizationAccountAddress(registrationModel.getOrganizationAddress(), orgAccountEntity);

        activateLicense(orgAccountLicense);
        registerOrganizationAccountOwner(registrationModel.getAccountOwner(), orgAccountEntity);
        createOrganizationAccountProfile(orgAccountEntity, registrationModel.getBusinessPhoneNumber());
        createInventory(orgAccountEntity);
        createNoteCredits(orgAccountEntity);
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

    private void createOrganizationAccountAddress(PortalAddressModel addressModel, OrganizationAccountEntity organizationAccount) {
        OrganizationAccountAddressEntity orgAcctAddressToSave = new OrganizationAccountAddressEntity(organizationAccount.getOrganizationAccountId());
        addressModel.copyFieldsToEntity(orgAcctAddressToSave);
        organizationAccountAddressRepository.save(orgAcctAddressToSave);
    }

    private void registerOrganizationAccountOwner(OrganizationAccountUserRegistrationModel accountOwnerModel, OrganizationAccountEntity organizationAccount) {
        UserRegistrationModel organizationAccountOwnerToRegister = new UserRegistrationModel(
                accountOwnerModel.getFirstName(), accountOwnerModel.getLastName(), accountOwnerModel.getEmail(), accountOwnerModel.getPassword(), PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER);
        UserEntity orgAcctOwnerUserEntity = userRegistrationService.registerOrganizationUser(organizationAccountOwnerToRegister, organizationAccount);

        UserProfileUpdateModel accountOwnerProfileUpdateModel = new UserProfileUpdateModel(
                accountOwnerModel.getFirstName(), accountOwnerModel.getLastName(), accountOwnerModel.getEmail(),
                accountOwnerModel.getUserAddress(), accountOwnerModel.getMobilePhoneNumber(), accountOwnerModel.getBusinessPhoneNumber());
        userProfileService.updateProfileWithoutPermissionsCheck(orgAcctOwnerUserEntity.getUserId(), accountOwnerProfileUpdateModel);
    }

    private void createOrganizationAccountProfile(OrganizationAccountEntity organizationAccount, String businessPhoneNumber) {
        OrganizationAccountProfileEntity orgAccountProfileToSave = new OrganizationAccountProfileEntity(null, organizationAccount.getOrganizationAccountId(), businessPhoneNumber);
        organizationAccountProfileRepository.save(orgAccountProfileToSave);
    }

    private void createInventory(OrganizationAccountEntity orgAccountEntity) {
        InventoryEntity orgAcctInventoryToSave = new InventoryEntity();
        InventoryEntity savedInventory = inventoryRepository.save(orgAcctInventoryToSave);
        InventoryOrganizationAccountRestrictionEntity restrictionToSave = new InventoryOrganizationAccountRestrictionEntity(savedInventory.getInventoryId(), orgAccountEntity.getOrganizationAccountId());
        inventoryOrgAcctRestrictionRepository.save(restrictionToSave);
    }

    private void createNoteCredits(OrganizationAccountEntity orgAccountEntity) {
        NoteCreditEntity noteCreditsToSave = new NoteCreditEntity(null, 0);
        NoteCreditEntity savedNoteCredits = noteCreditRepository.save(noteCreditsToSave);
        NoteCreditOrgAccountRestrictionEntity restrictionToSave = new NoteCreditOrgAccountRestrictionEntity(savedNoteCredits.getNoteCreditId(), orgAccountEntity.getOrganizationAccountId());
        noteCreditOrgAccountRestrictionRepository.save(restrictionToSave);
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
