package com.usepipeline.portal.web.registration.user;

import com.usepipeline.portal.common.enumeration.PortalRole;
import com.usepipeline.portal.common.exception.PortalException;
import com.usepipeline.portal.database.authentication.entity.*;
import com.usepipeline.portal.database.authentication.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Optional;

@Component
@Slf4j
public class UserRegistrationService {
    private UserRepository userRepository;
    private LoginRepository loginRepository;
    private RoleRepository roleRepository;
    private OrganizationAccountRepository organizationAccountRepository;
    private MembershipRepository membershipRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserRegistrationService(UserRepository userRepository, LoginRepository loginRepository,
                                   RoleRepository roleRepository, OrganizationAccountRepository organizationAccountRepository,
                                   MembershipRepository membershipRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
        this.roleRepository = roleRepository;
        this.organizationAccountRepository = organizationAccountRepository;
        this.membershipRepository = membershipRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public boolean registerUser(UserRegistrationModel registrationModel) {
        boolean valid = validateRegistrationModel(registrationModel);
        if (valid) {
            return registerValidUser(registrationModel);
        }
        log.error("The registration request was invalid");
        return false;
    }

    private boolean registerValidUser(UserRegistrationModel registrationModel) {
        try {
            UserEntity userEntity = saveUserInfo(registrationModel.getFirstName(), registrationModel.getLastName(), registrationModel.getEmail());
            OrganizationAccountEntity organizationAccountEntity = savePlanInfo(registrationModel.getPlanType());

            RoleEntity roleEntity = getRoleInfo(registrationModel.getPlanType());
            saveLoginInfo(userEntity.getUserId(), registrationModel.getPassword());
            saveMembershipInfo(userEntity.getUserId(), organizationAccountEntity.getOrganizationAccountId(), roleEntity.getRoleId());
        } catch (PortalException e) {
            log.error("There was a problem registering the user", e);
            return false;
        }
        return true;
    }

    private UserEntity saveUserInfo(String firstName, String lastName, String email) throws PortalException {
        Optional<UserEntity> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new PortalException("A user with the email '" + email + "' already exists");
        }

        UserEntity newUserToSave = new UserEntity(null, email, firstName, lastName, true);
        return userRepository.save(newUserToSave);
    }

    private OrganizationAccountEntity savePlanInfo(String planName) throws PortalException {
        return organizationAccountRepository.findFirstByOrganizationAccountName(planName)
                .orElseThrow(() -> new PortalException("No plan with the name '" + planName + "' exists"));
    }

    private RoleEntity getRoleInfo(String planName) throws PortalException {
        PortalRole portalRole = PortalRole.PIPELINE_BASIC_USER;
        // TODO think of a better way to map this
        if (planName.toLowerCase().contains("premium")) {
            portalRole = PortalRole.PIPELINE_PREMIUM_USER;
        }

        return roleRepository.findRoleByRoleLevel(portalRole.name())
                .orElseThrow(() -> new PortalException("No role for the plan '" + planName + "' exists"));
    }

    private void saveLoginInfo(Long userId, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        LoginEntity newLoginToSave = new LoginEntity(null, userId, encodedPassword, null, 0);
        loginRepository.save(newLoginToSave);
    }

    private void saveMembershipInfo(Long userId, Long organizationAccountId, Long roleId) {
        MembershipEntity newMembershipToSave = new MembershipEntity(null, userId, organizationAccountId, roleId, true);
        membershipRepository.save(newMembershipToSave);
    }

    private boolean validateRegistrationModel(UserRegistrationModel registrationModel) {
        boolean valid = true;
        valid &= isBlankLogError("firstName", registrationModel.getFirstName());
        valid &= isBlankLogError("lastName", registrationModel.getLastName());
        valid &= isBlankLogError("email", registrationModel.getEmail());
        valid &= isBlankLogError("password", registrationModel.getPassword());
        valid &= isBlankLogError("planType", registrationModel.getPlanType());
        return valid;
    }

    private boolean isBlankLogError(String fieldName, String fieldValue) {
        if (StringUtils.isBlank(fieldValue)) {
            log.error("The field '{}' cannot be blank", fieldName);
            return false;
        }
        return true;
    }

}
