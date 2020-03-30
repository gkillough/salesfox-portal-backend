package com.usepipeline.portal.web.registration.user;

import com.usepipeline.portal.database.authentication.entity.*;
import com.usepipeline.portal.database.authentication.repository.*;
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

    @Transactional
    public boolean registerUser(UserRegistrationModel registrationModel) {
        validateRegistrationModel(registrationModel);
        return registerValidUser(registrationModel);
    }

    private boolean registerValidUser(UserRegistrationModel registrationModel) {
        UserEntity userEntity = saveUserInfo(registrationModel.getFirstName(), registrationModel.getLastName(), registrationModel.getEmail());
        OrganizationAccountEntity organizationAccountEntity = savePlanInfo(registrationModel.getPlanType());

        RoleEntity roleEntity = getRoleInfo(registrationModel.getPlanType());
        saveLoginInfo(userEntity.getUserId(), registrationModel.getPassword());
        saveMembershipInfo(userEntity.getUserId(), organizationAccountEntity.getOrganizationAccountId(), roleEntity.getRoleId());
        profileRepository.initializeProfile(userEntity.getUserId());
        return true;
    }

    private UserEntity saveUserInfo(String firstName, String lastName, String email) {
        Optional<UserEntity> existingUser = userRepository.findFirstByEmail(email);
        if (existingUser.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A user with the email '[" + email + "]' already exists");
        }

        UserEntity newUserToSave = new UserEntity(null, email, firstName, lastName, true);
        return userRepository.save(newUserToSave);
    }

    private OrganizationAccountEntity savePlanInfo(String planName) {
        return organizationAccountRepository.findFirstByOrganizationAccountName(planName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No plan with the name '[" + planName + "]' exists"));
    }

    private RoleEntity getRoleInfo(String planName) {
        String portalRole = PortalAuthorityConstants.PIPELINE_BASIC_USER;
        // TODO think of a better way to map this
        if (planName.toLowerCase().contains("premium")) {
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
        isBlankBadRequest(errorFields, "First Name", registrationModel.getFirstName());
        isBlankBadRequest(errorFields, "Last Name", registrationModel.getLastName());
        isBlankBadRequest(errorFields, "Email", registrationModel.getEmail());
        isBlankBadRequest(errorFields, "Password", registrationModel.getPassword());
        isBlankBadRequest(errorFields, "Plan Type", registrationModel.getPlanType());

        if (!errorFields.isEmpty()) {
            String commaSeparatedErrors = String.join(", ", errorFields);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The field(s) [%s] cannot be blank", commaSeparatedErrors));
        }
    }

    private void isBlankBadRequest(List<String> errorFields, String fieldName, String fieldValue) {
        if (StringUtils.isBlank(fieldValue)) {
            errorFields.add(String.format("'%s'", fieldName));
        }
    }

}
