package ai.salesfox.portal.rest.api.registration.user;

import ai.salesfox.portal.common.FieldValidationUtils;
import ai.salesfox.portal.common.service.license.LicenseSeatManager;
import ai.salesfox.portal.database.account.entity.LoginEntity;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.RoleEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.account.repository.LoginRepository;
import ai.salesfox.portal.database.account.repository.MembershipRepository;
import ai.salesfox.portal.database.account.repository.RoleRepository;
import ai.salesfox.portal.database.account.repository.UserRepository;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import ai.salesfox.portal.rest.api.user.profile.UserProfileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    private final MembershipRepository membershipRepository;
    private final UserProfileService userProfileService;
    private final LicenseSeatManager licenseSeatManager;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserRegistrationService(UserRepository userRepository,
                                   LoginRepository loginRepository,
                                   RoleRepository roleRepository,
                                   MembershipRepository membershipRepository,
                                   UserProfileService userProfileService,
                                   LicenseSeatManager licenseSeatManager,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
        this.roleRepository = roleRepository;
        this.membershipRepository = membershipRepository;
        this.userProfileService = userProfileService;
        this.licenseSeatManager = licenseSeatManager;
        this.passwordEncoder = passwordEncoder;
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
        OrganizationAccountLicenseEntity orgLicense = orgAccount.getOrganizationAccountLicenseEntity();
        licenseSeatManager.fillSeat(orgLicense);
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

    private void validateRegistrationModel(UserRegistrationModel registrationModel) {
        List<String> errorFields = new ArrayList<>();
        isBlankAddError(errorFields, "First Name", registrationModel.getFirstName());
        isBlankAddError(errorFields, "Last Name", registrationModel.getLastName());
        isBlankAddError(errorFields, "Email", registrationModel.getEmail());
        isBlankAddError(errorFields, "Password", registrationModel.getPassword());
        isBlankAddError(errorFields, "Role", registrationModel.getRoleLevel());

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
