package com.usepipeline.portal.web.registration.user;

import com.usepipeline.portal.common.enumeration.PortalRoles;
import com.usepipeline.portal.common.exception.PortalException;
import com.usepipeline.portal.database.authentication.entity.*;
import com.usepipeline.portal.database.authentication.repository.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Optional;

@Component
public class UserRegistrationService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

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
        logger.error("The registration request was invalid");
        return false;
    }

    private boolean registerValidUser(UserRegistrationModel registrationModel) {
        try {
            UserEntity userEntity = saveUserInfo(registrationModel.getFirstName(), registrationModel.getLastName(), registrationModel.getEmail());
            OrganizationAccountEntity organizationAccountEntity = savePlanInfo(registrationModel.getPlanType());

            saveLoginInfo(userEntity.getUserId(), registrationModel.getPassword());
            saveMembershipInfo(userEntity.getUserId(), organizationAccountEntity.getOrganizationAccountId());
        } catch (PortalException e) {
            logger.error("There was a problem registering the user", e);
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
        return organizationAccountRepository.findByOrganizationAccountName(planName)
                .orElseThrow(() -> new PortalException("No plan with the name '" + planName + "' exists"));
    }

    private void saveLoginInfo(Long userId, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        LoginEntity newLoginToSave = new LoginEntity(null, userId, "", encodedPassword, null, 0);
        loginRepository.save(newLoginToSave);
    }

    private void saveMembershipInfo(Long userId, Long organizationAccountId) throws PortalException {
        Long roleId = roleRepository.findRoleByRoleLevel(PortalRoles.ORGANIZATION_SALES_REP.name())
                .map(RoleEntity::getRoleId)
                .orElseThrow(() -> new PortalException("Plan not supported"));
        MembershipEntity
                newMembershipToSave = new MembershipEntity(null, userId, organizationAccountId, roleId, true);
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
            logger.error("The field '{}' cannot be blank", fieldName);
            return false;
        }
        return true;
    }

}
