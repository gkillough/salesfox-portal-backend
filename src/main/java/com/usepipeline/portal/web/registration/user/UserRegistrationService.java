package com.usepipeline.portal.web.registration.user;

import com.usepipeline.portal.database.account.entity.*;
import com.usepipeline.portal.database.account.repository.*;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import com.usepipeline.portal.web.user.profile.UserProfileService;
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

@Component
@Slf4j
public class UserRegistrationService {
    private UserRepository userRepository;
    private LoginRepository loginRepository;
    private RoleRepository roleRepository;
    private OrganizationAccountRepository organizationAccountRepository;
    private MembershipRepository membershipRepository;
    private UserProfileService profileRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserRegistrationService(UserRepository userRepository, LoginRepository loginRepository,
                                   RoleRepository roleRepository, OrganizationAccountRepository organizationAccountRepository,
                                   MembershipRepository membershipRepository, UserProfileService profileRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
        this.roleRepository = roleRepository;
        this.organizationAccountRepository = organizationAccountRepository;
        this.membershipRepository = membershipRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @param registrationModel  a model containing the fields required to register a user
     * @param isOrganizationUser a flag to indicate whether the user being created is for an organization
     * @return the id of the registered user
     */
    @Transactional
    public Long registerUser(UserRegistrationModel registrationModel, boolean isOrganizationUser) {
        validateRegistrationModel(registrationModel);
        return registerValidUser(registrationModel, isOrganizationUser);
    }

    private Long registerValidUser(UserRegistrationModel registrationModel, boolean isOrganizationUser) {
        UserEntity userEntity = saveUserInfo(registrationModel.getFirstName(), registrationModel.getLastName(), registrationModel.getEmail());
        OrganizationAccountEntity organizationAccountEntity = savePlanInfo(registrationModel.getPlanType(), isOrganizationUser);

        RoleEntity roleEntity = getRoleInfo(registrationModel.getPlanType(), isOrganizationUser);
        saveLoginInfo(userEntity.getUserId(), registrationModel.getPassword());
        saveMembershipInfo(userEntity.getUserId(), organizationAccountEntity.getOrganizationAccountId(), roleEntity.getRoleId());
        profileRepository.initializeProfile(userEntity.getUserId());
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

    private OrganizationAccountEntity savePlanInfo(String planName, boolean isOrganizationUser) {
        if (!isOrganizationUser) {
            // TODO make this consistent with the UI, then use constants
            if (!planName.toLowerCase().contains("basic") && !planName.toLowerCase().contains("premium")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid plan selected. To create a business account please contact us.");
            }
        }
        return organizationAccountRepository.findFirstByOrganizationAccountName(planName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No plan with the name '[" + planName + "]' exists"));
    }

    private RoleEntity getRoleInfo(String planName, boolean isOrganizationUser) {
        String portalRole = PortalAuthorityConstants.PIPELINE_BASIC_USER;
        if (isOrganizationUser) {
            portalRole = PortalAuthorityConstants.ORGANIZATION_ACCOUNT_MANAGER;
        }
        // TODO make this consistent with the UI, then use constants
        else if (planName.toLowerCase().contains("premium")) {
            portalRole = PortalAuthorityConstants.PIPELINE_PREMIUM_USER;
        }

        return roleRepository.findFirstByRoleLevel(portalRole)
                .orElseThrow(() -> {
                    log.warn("No role for the plan '[" + planName + "]' exists");
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST);
                });
    }

    private void saveLoginInfo(Long userId, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        LoginEntity newLoginToSave = new LoginEntity(null, userId, encodedPassword, null, null, 0);
        loginRepository.save(newLoginToSave);
    }

    private void saveMembershipInfo(Long userId, Long organizationAccountId, Long roleId) {
        MembershipEntity newMembershipToSave = new MembershipEntity(null, userId, organizationAccountId, roleId, true);
        membershipRepository.save(newMembershipToSave);
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
    }

    private void isBlankAddError(List<String> errorFields, String fieldName, String fieldValue) {
        if (StringUtils.isBlank(fieldValue)) {
            errorFields.add(String.format("'%s'", fieldName));
        }
    }

}
