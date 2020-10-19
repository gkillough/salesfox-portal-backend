package ai.salesfox.portal.rest.api.registration.organization;

import ai.salesfox.portal.common.FieldValidationUtils;
import ai.salesfox.portal.common.model.PortalAddressModel;
import ai.salesfox.portal.common.time.PortalDateTimeUtils;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.inventory.InventoryEntity;
import ai.salesfox.portal.database.inventory.InventoryRepository;
import ai.salesfox.portal.database.inventory.restriction.InventoryOrganizationAccountRestrictionEntity;
import ai.salesfox.portal.database.inventory.restriction.InventoryOrganizationAccountRestrictionRepository;
import ai.salesfox.portal.database.license.LicenseTypeEntity;
import ai.salesfox.portal.database.license.LicenseTypeRepository;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseEntity;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseRepository;
import ai.salesfox.portal.database.note.credit.NoteCreditOrgAccountRestrictionEntity;
import ai.salesfox.portal.database.note.credit.NoteCreditsEntity;
import ai.salesfox.portal.database.note.credit.NoteCreditsOrgAccountRestrictionRepository;
import ai.salesfox.portal.database.note.credit.NoteCreditsRepository;
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
    private final LicenseTypeRepository licenseTypeRepository;
    private final OrganizationAccountLicenseRepository organizationAccountLicenseRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationAccountRepository organizationAccountRepository;
    private final OrganizationAccountAddressRepository organizationAccountAddressRepository;
    private final OrganizationAccountProfileRepository organizationAccountProfileRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryOrganizationAccountRestrictionRepository inventoryOrgAcctRestrictionRepository;
    private final NoteCreditsRepository noteCreditsRepository;
    private final NoteCreditsOrgAccountRestrictionRepository noteCreditsOrgAccountRestrictionRepository;
    private final OrganizationValidationService organizationValidationService;
    private final UserRegistrationService userRegistrationService;
    private final UserProfileService userProfileService;

    @Autowired
    public OrganizationAccountRegistrationService(LicenseTypeRepository licenseTypeRepository,
                                                  OrganizationAccountLicenseRepository organizationAccountLicenseRepository,
                                                  OrganizationRepository organizationRepository,
                                                  OrganizationAccountRepository organizationAccountRepository,
                                                  OrganizationAccountAddressRepository organizationAccountAddressRepository,
                                                  OrganizationAccountProfileRepository organizationAccountProfileRepository,
                                                  InventoryRepository inventoryRepository,
                                                  InventoryOrganizationAccountRestrictionRepository inventoryOrgAcctRestrictionRepository,
                                                  NoteCreditsRepository noteCreditsRepository,
                                                  NoteCreditsOrgAccountRestrictionRepository noteCreditsOrgAccountRestrictionRepository,
                                                  OrganizationValidationService organizationValidationService,
                                                  UserRegistrationService userRegistrationService,
                                                  UserProfileService userProfileService
    ) {
        this.licenseTypeRepository = licenseTypeRepository;
        this.organizationAccountLicenseRepository = organizationAccountLicenseRepository;
        this.organizationRepository = organizationRepository;
        this.organizationAccountRepository = organizationAccountRepository;
        this.organizationAccountAddressRepository = organizationAccountAddressRepository;
        this.organizationAccountProfileRepository = organizationAccountProfileRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryOrgAcctRestrictionRepository = inventoryOrgAcctRestrictionRepository;
        this.noteCreditsRepository = noteCreditsRepository;
        this.noteCreditsOrgAccountRestrictionRepository = noteCreditsOrgAccountRestrictionRepository;
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
        LicenseTypeEntity licenseType = getAndValidateLicenseByHash(registrationModel.getLicenseTypeId());
        validateRegistrationFields(registrationModel);

        OrganizationEntity orgEntity = getOrCreateOrganizationWithName(registrationModel.getOrganizationName());
        OrganizationAccountEntity orgAccountEntity = createOrganizationAccount(registrationModel, orgEntity);
        createOrganizationAccountLicense(orgAccountEntity, licenseType);
        createOrganizationAccountAddress(registrationModel.getOrganizationAddress(), orgAccountEntity);

        registerOrganizationAccountOwner(registrationModel.getAccountOwner(), orgAccountEntity);
        createOrganizationAccountProfile(orgAccountEntity, registrationModel.getBusinessPhoneNumber());
        createInventory(orgAccountEntity);
        createNoteCredits(orgAccountEntity);
    }

    private LicenseTypeEntity getAndValidateLicenseByHash(UUID licenseTypeId) {
        if (licenseTypeId != null) {
            Optional<LicenseTypeEntity> existingLicense = licenseTypeRepository.findById(licenseTypeId);
            if (existingLicense.isPresent()) {
                return existingLicense.get();
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

    private OrganizationAccountEntity createOrganizationAccount(OrganizationAccountRegistrationModel registrationModel, OrganizationEntity organization) {
        OrganizationAccountEntity orgAccountToSave = new OrganizationAccountEntity(
                null, registrationModel.getOrganizationAccountName(), organization.getOrganizationId(), true);
        return organizationAccountRepository.save(orgAccountToSave);
    }

    private void createOrganizationAccountLicense(OrganizationAccountEntity organizationAccount, LicenseTypeEntity licenseType) {
        int currentDayOfMonth = PortalDateTimeUtils.getCurrentDate().getDayOfMonth();
        // TODO extract this into a common interface
        //  7-day free trial period before first bill adjusted for valid billing days (days 1-28 of any month)
        int billingDayOfMonth = ((currentDayOfMonth + 6) % 27) + 1;

        OrganizationAccountLicenseEntity orgAcctLicenseToSave = new OrganizationAccountLicenseEntity(
                organizationAccount.getOrganizationAccountId(), licenseType.getLicenseTypeId(), 0, billingDayOfMonth, true);
        OrganizationAccountLicenseEntity savedOrgAcctLicense = organizationAccountLicenseRepository.save(orgAcctLicenseToSave);
        organizationAccount.setOrganizationAccountLicenseEntity(savedOrgAcctLicense);
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
        NoteCreditsEntity noteCreditsToSave = new NoteCreditsEntity(null, 0);
        NoteCreditsEntity savedNoteCredits = noteCreditsRepository.save(noteCreditsToSave);
        NoteCreditOrgAccountRestrictionEntity restrictionToSave = new NoteCreditOrgAccountRestrictionEntity(savedNoteCredits.getNoteCreditId(), orgAccountEntity.getOrganizationAccountId());
        noteCreditsOrgAccountRestrictionRepository.save(restrictionToSave);
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
