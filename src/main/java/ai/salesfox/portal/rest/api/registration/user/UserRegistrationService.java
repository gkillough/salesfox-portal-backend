package ai.salesfox.portal.rest.api.registration.user;

import ai.salesfox.portal.common.FieldValidationUtils;
import ai.salesfox.portal.common.exception.PortalDatabaseIntegrityViolationException;
import ai.salesfox.portal.common.service.license.LicenseSeatManager;
import ai.salesfox.portal.common.service.license.PortalLicenseSeatException;
import ai.salesfox.portal.database.account.entity.*;
import ai.salesfox.portal.database.account.repository.LoginRepository;
import ai.salesfox.portal.database.account.repository.MembershipRepository;
import ai.salesfox.portal.database.account.repository.RoleRepository;
import ai.salesfox.portal.database.account.repository.UserRepository;
import ai.salesfox.portal.database.inventory.InventoryEntity;
import ai.salesfox.portal.database.inventory.InventoryRepository;
import ai.salesfox.portal.database.inventory.restriction.InventoryUserRestrictionEntity;
import ai.salesfox.portal.database.inventory.restriction.InventoryUserRestrictionRepository;
import ai.salesfox.portal.database.note.credit.NoteCreditEntity;
import ai.salesfox.portal.database.note.credit.NoteCreditRepository;
import ai.salesfox.portal.database.note.credit.NoteCreditUserRestrictionEntity;
import ai.salesfox.portal.database.note.credit.NoteCreditUserRestrictionRepository;
import ai.salesfox.portal.database.organization.OrganizationEntity;
import ai.salesfox.portal.database.organization.OrganizationRepository;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountRepository;
import ai.salesfox.portal.rest.api.registration.organization.OrganizationConstants;
import ai.salesfox.portal.rest.api.user.profile.UserProfileService;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class UserRegistrationService {
    private final UserRepository userRepository;
    private final LoginRepository loginRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationAccountRepository organizationAccountRepository;
    private final MembershipRepository membershipRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryUserRestrictionRepository inventoryUserRestrictionRepository;
    private final NoteCreditRepository noteCreditRepository;
    private final NoteCreditUserRestrictionRepository noteCreditUserRestrictionRepository;
    private final UserProfileService userProfileService;
    private final LicenseSeatManager licenseSeatManager;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserRegistrationService(UserRepository userRepository,
                                   LoginRepository loginRepository,
                                   RoleRepository roleRepository,
                                   OrganizationRepository organizationRepository,
                                   OrganizationAccountRepository organizationAccountRepository,
                                   MembershipRepository membershipRepository,
                                   InventoryRepository inventoryRepository,
                                   InventoryUserRestrictionRepository inventoryUserRestrictionRepository,
                                   NoteCreditRepository noteCreditRepository,
                                   NoteCreditUserRestrictionRepository noteCreditUserRestrictionRepository,
                                   UserProfileService userProfileService,
                                   LicenseSeatManager licenseSeatManager,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
        this.organizationAccountRepository = organizationAccountRepository;
        this.membershipRepository = membershipRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryUserRestrictionRepository = inventoryUserRestrictionRepository;
        this.noteCreditRepository = noteCreditRepository;
        this.noteCreditUserRestrictionRepository = noteCreditUserRestrictionRepository;
        this.userProfileService = userProfileService;
        this.licenseSeatManager = licenseSeatManager;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @param registrationModel a model containing the fields required to register a user
     * @return the id of the registered user
     */
    @Transactional
    public void registerUser(UserRegistrationModel registrationModel) {
        UUID defaultOrganizationId = organizationRepository.findFirstByOrganizationName(OrganizationConstants.PLAN_INDIVIDUAL_ORG_NAME)
                .map(OrganizationEntity::getOrganizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

        String roleLevel = registrationModel.getRoleLevel();
        String planName;
        if (null == roleLevel) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid plan selected");
        } else if (roleLevel.equals(PortalAuthorityConstants.PORTAL_PREMIUM_USER)) {
            planName = OrganizationConstants.PLAN_INDIVIDUAL_PREMIUM_DISPLAY_NAME;
        } else {
            planName = OrganizationConstants.PLAN_INDIVIDUAL_BASIC_DISPLAY_NAME;
        }

        OrganizationAccountEntity individualOrgAccount = organizationAccountRepository.findFirstByOrganizationIdAndOrganizationAccountName(defaultOrganizationId, planName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No plan with the name '[" + planName + "]' exists"));
        registerOrganizationUser(registrationModel, individualOrgAccount);
    }

    /**
     * @param registrationModel a model containing the fields required to register a user
     * @param orgAccount        the organization account in which to register the user
     * @return the id of the registered user
     */
    @Transactional
    public UserEntity registerOrganizationUser(UserRegistrationModel registrationModel, OrganizationAccountEntity orgAccount) {
        validateRegistrationModel(registrationModel);
        return registerValidatedUser(registrationModel, orgAccount);
    }

    private UserEntity registerValidatedUser(UserRegistrationModel registrationModel, OrganizationAccountEntity orgAccount) {
        UserEntity userEntity = saveUserInfo(registrationModel.getFirstName(), registrationModel.getLastName(), registrationModel.getEmail());
        RoleEntity roleEntity = getRoleInfo(registrationModel.getRoleLevel());

        reserveLicenseSeatForNewUser(orgAccount);
        LoginEntity loginInfo = saveLoginInfo(userEntity.getUserId(), registrationModel.getPassword());
        MembershipEntity membershipInfo = saveMembershipInfo(userEntity.getUserId(), orgAccount.getOrganizationAccountId(), roleEntity.getRoleId());

        createInventoryIfNecessary(userEntity.getUserId(), roleEntity);
        createNoteCreditsIfNecessary(userEntity.getUserId(), roleEntity);
        userProfileService.initializeProfile(userEntity.getUserId());

        userEntity.setLoginEntity(loginInfo);
        userEntity.setMembershipEntity(membershipInfo);
        return userEntity;
    }

    private UserEntity saveUserInfo(String firstName, String lastName, String email) {
        Optional<UserEntity> existingUser = userRepository.findFirstByEmail(email);
        if (existingUser.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A user with the email '[" + email + "]' already exists");
        }

        UserEntity newUserToSave = new UserEntity(null, email, firstName, lastName, true);
        return userRepository.save(newUserToSave);
    }

    private RoleEntity getRoleInfo(String roleLevel) {
        return roleRepository.findFirstByRoleLevel(roleLevel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private void reserveLicenseSeatForNewUser(OrganizationAccountEntity orgAccount) {
        try {
            LicenseEntity orgLicense = licenseSeatManager.getLicenseForOrganizationAccount(orgAccount);
            licenseSeatManager.fillSeat(orgLicense);
        } catch (PortalDatabaseIntegrityViolationException e) {
            log.error("There was a problem managing the organization license", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (PortalLicenseSeatException e) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, e.getMessage());
        }
    }

    private LoginEntity saveLoginInfo(UUID userId, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        LoginEntity newLoginToSave = new LoginEntity(userId, encodedPassword, null, null, 0);
        return loginRepository.save(newLoginToSave);
    }

    private MembershipEntity saveMembershipInfo(UUID userId, UUID organizationAccountId, UUID roleId) {
        MembershipEntity newMembershipToSave = new MembershipEntity(userId, organizationAccountId, roleId);
        return membershipRepository.save(newMembershipToSave);
    }

    private void createInventoryIfNecessary(UUID userId, RoleEntity roleEntity) {
        String roleLevel = roleEntity.getRoleLevel();
        if (PortalAuthorityConstants.PORTAL_BASIC_USER.equals(roleLevel) || PortalAuthorityConstants.PORTAL_PREMIUM_USER.equals(roleLevel)) {
            InventoryEntity individualUserInventoryToSave = new InventoryEntity();
            InventoryEntity savedInventory = inventoryRepository.save(individualUserInventoryToSave);
            InventoryUserRestrictionEntity restrictionToSave = new InventoryUserRestrictionEntity(savedInventory.getInventoryId(), userId);
            inventoryUserRestrictionRepository.save(restrictionToSave);
        }
    }

    private void createNoteCreditsIfNecessary(UUID userId, RoleEntity roleEntity) {
        String roleLevel = roleEntity.getRoleLevel();
        if (PortalAuthorityConstants.PORTAL_BASIC_USER.equals(roleLevel) || PortalAuthorityConstants.PORTAL_PREMIUM_USER.equals(roleLevel)) {
            NoteCreditEntity noteCreditsToSave = new NoteCreditEntity();
            NoteCreditEntity savedNoteCredits = noteCreditRepository.save(noteCreditsToSave);
            NoteCreditUserRestrictionEntity restrictionToSave = new NoteCreditUserRestrictionEntity(savedNoteCredits.getNoteCreditId(), userId);
            noteCreditUserRestrictionRepository.save(restrictionToSave);
        }
    }

    private void validateRegistrationModel(UserRegistrationModel registrationModel) {
        List<String> errorFields = new ArrayList<>();
        isBlankAddError(errorFields, "First Name", registrationModel.getFirstName());
        isBlankAddError(errorFields, "Last Name", registrationModel.getLastName());
        isBlankAddError(errorFields, "Email", registrationModel.getEmail());
        isBlankAddError(errorFields, "Password", registrationModel.getPassword());
        isBlankAddError(errorFields, "Plan", registrationModel.getRoleLevel());

        if (!errorFields.isEmpty()) {
            String commaSeparatedErrors = String.join(", ", errorFields);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The field(s) [%s] cannot be blank", commaSeparatedErrors));
        }

        if (!FieldValidationUtils.isValidEmailAddress(registrationModel.getEmail(), false)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The email [%s] is invalid", registrationModel.getEmail()));
        }
    }

    private void isBlankAddError(List<String> errorFields, String fieldName, String fieldValue) {
        if (StringUtils.isBlank(fieldValue)) {
            errorFields.add(String.format("'%s'", fieldName));
        }
    }

}
