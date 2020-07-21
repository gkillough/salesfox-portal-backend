package com.getboostr.portal.web.registration.user;

import com.getboostr.portal.common.FieldValidationUtils;
import com.getboostr.portal.common.exception.PortalDatabaseIntegrityViolationException;
import com.getboostr.portal.common.service.license.LicenseSeatManager;
import com.getboostr.portal.common.service.license.PortalLicenseSeatException;
import com.getboostr.portal.database.account.entity.*;
import com.getboostr.portal.database.account.repository.LoginRepository;
import com.getboostr.portal.database.account.repository.MembershipRepository;
import com.getboostr.portal.database.account.repository.RoleRepository;
import com.getboostr.portal.database.account.repository.UserRepository;
import com.getboostr.portal.database.inventory.InventoryEntity;
import com.getboostr.portal.database.inventory.InventoryRepository;
import com.getboostr.portal.database.organization.OrganizationEntity;
import com.getboostr.portal.database.organization.OrganizationRepository;
import com.getboostr.portal.database.organization.account.OrganizationAccountEntity;
import com.getboostr.portal.web.registration.organization.OrganizationConstants;
import com.usepipeline.portal.database.account.entity.*;
import com.getboostr.portal.database.organization.account.OrganizationAccountRepository;
import com.getboostr.portal.web.security.authorization.PortalAuthorityConstants;
import com.getboostr.portal.web.user.profile.UserProfileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
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
    private UserRepository userRepository;
    private LoginRepository loginRepository;
    private RoleRepository roleRepository;
    private OrganizationRepository organizationRepository;
    private OrganizationAccountRepository organizationAccountRepository;
    private MembershipRepository membershipRepository;
    private InventoryRepository inventoryRepository;
    private UserProfileService userProfileService;
    private LicenseSeatManager licenseSeatManager;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserRegistrationService(UserRepository userRepository, LoginRepository loginRepository, RoleRepository roleRepository,
                                   OrganizationRepository organizationRepository, OrganizationAccountRepository organizationAccountRepository, MembershipRepository membershipRepository,
                                   InventoryRepository inventoryRepository, UserProfileService userProfileService, LicenseSeatManager licenseSeatManager, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
        this.organizationAccountRepository = organizationAccountRepository;
        this.membershipRepository = membershipRepository;
        this.inventoryRepository = inventoryRepository;
        this.userProfileService = userProfileService;
        this.licenseSeatManager = licenseSeatManager;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @param registrationModel a model containing the fields required to register a user
     * @return the id of the registered user
     */
    @Transactional
    public UUID registerUser(UserRegistrationModel registrationModel) {
        UUID defaultPipelineOrganizationId = organizationRepository.findFirstByOrganizationName(OrganizationConstants.PLAN_PIPELINE_BASIC_OR_PREMIUM_DEFAULT_ORG_NAME)
                .map(OrganizationEntity::getOrganizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        return registerOrganizationUser(registrationModel, defaultPipelineOrganizationId, null);
    }

    /**
     * @param registrationModel a model containing the fields required to register a user
     * @param organizationId    an id to indicate which organization to assign to the user
     * @param role              a nullable role to indicate which role to assign to the user
     * @return the id of the registered user
     */
    @Transactional
    public UUID registerOrganizationUser(UserRegistrationModel registrationModel, UUID organizationId, @Nullable String role) {
        validateRegistrationModel(registrationModel);
        return registerValidOrganizationUser(registrationModel, organizationId, role);
    }

    private UUID registerValidOrganizationUser(UserRegistrationModel registrationModel, UUID organizationId, @Nullable String role) {
        UserEntity userEntity = saveUserInfo(registrationModel.getFirstName(), registrationModel.getLastName(), registrationModel.getEmail());

        RoleEntity roleEntity = getRoleInfo(registrationModel.getPlanType(), role);
        OrganizationAccountEntity organizationAccountEntity = getPlanInfo(registrationModel.getPlanType(), organizationId);

        reserveLicenseSeatForNewUser(organizationAccountEntity);
        saveLoginInfo(userEntity.getUserId(), registrationModel.getPassword());
        saveMembershipInfo(userEntity.getUserId(), organizationAccountEntity.getOrganizationAccountId(), roleEntity.getRoleId());

        createInventoryIfNecessary(userEntity.getUserId(), organizationAccountEntity.getOrganizationAccountId(), roleEntity);
        userProfileService.initializeProfile(userEntity.getUserId());
        return userEntity.getUserId();
    }

    private UserEntity saveUserInfo(String firstName, String lastName, String email) {
        Optional<UserEntity> existingUser = userRepository.findFirstByEmail(email);
        if (existingUser.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A user with the email '[" + email + "]' already exists");
        }

        UserEntity newUserToSave = new UserEntity(null, email, firstName, lastName, true);
        return userRepository.save(newUserToSave);
    }

    private OrganizationAccountEntity getPlanInfo(String planName, UUID organizationId) {
        return organizationAccountRepository.findFirstByOrganizationIdAndOrganizationAccountName(organizationId, planName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No plan with the name '[" + planName + "]' exists"));
    }

    private RoleEntity getRoleInfo(String planName, @Nullable String role) {
        String portalRole;
        if (role != null) {
            portalRole = role;
        } else if (OrganizationConstants.PLAN_PIPELINE_BASIC_DISPLAY_NAME.equals(planName)) {
            portalRole = PortalAuthorityConstants.PORTAL_BASIC_USER;
        } else if (OrganizationConstants.PLAN_PIPELINE_PREMIUM_DISPLAY_NAME.equals(planName)) {
            portalRole = PortalAuthorityConstants.PORTAL_PREMIUM_USER;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid plan selected. To create a business account please contact us.");
        }

        return roleRepository.findFirstByRoleLevel(portalRole)
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

    private void saveLoginInfo(UUID userId, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        LoginEntity newLoginToSave = new LoginEntity(null, userId, encodedPassword, null, null, 0);
        loginRepository.save(newLoginToSave);
    }

    private void saveMembershipInfo(UUID userId, UUID organizationAccountId, UUID roleId) {
        MembershipEntity newMembershipToSave = new MembershipEntity(null, userId, organizationAccountId, roleId);
        membershipRepository.save(newMembershipToSave);
    }

    private void createInventoryIfNecessary(UUID userId, UUID orgAcctId, RoleEntity roleEntity) {
        String roleLevel = roleEntity.getRoleLevel();
        if (PortalAuthorityConstants.PORTAL_BASIC_USER.equals(roleLevel) || PortalAuthorityConstants.PORTAL_PREMIUM_USER.equals(roleLevel)) {
            InventoryEntity individualUserInventoryToSave = new InventoryEntity(null, orgAcctId, userId);
            inventoryRepository.save(individualUserInventoryToSave);
        }
    }

    private void validateRegistrationModel(UserRegistrationModel registrationModel) {
        List<String> errorFields = new ArrayList<>();
        isBlankAddError(errorFields, "First Name", registrationModel.getFirstName());
        isBlankAddError(errorFields, "Last Name", registrationModel.getLastName());
        isBlankAddError(errorFields, "Email", registrationModel.getEmail());
        isBlankAddError(errorFields, "Password", registrationModel.getPassword());
        isBlankAddError(errorFields, "Plan Type", registrationModel.getPlanType());

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
