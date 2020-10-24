package ai.salesfox.portal.rest.api.organization.invitation;

import ai.salesfox.portal.PortalConfiguration;
import ai.salesfox.portal.common.FieldValidationUtils;
import ai.salesfox.portal.common.service.email.EmailMessagingService;
import ai.salesfox.portal.common.service.email.PortalEmailException;
import ai.salesfox.portal.common.service.email.model.ButtonEmailMessageModel;
import ai.salesfox.portal.common.service.email.model.EmailMessageModel;
import ai.salesfox.portal.common.time.PortalDateTimeUtils;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.account.repository.RoleRepository;
import ai.salesfox.portal.database.account.repository.UserRepository;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountRepository;
import ai.salesfox.portal.database.organization.account.invite.OrganizationAccountInviteTokenEntity;
import ai.salesfox.portal.database.organization.account.invite.OrganizationAccountInviteTokenPK;
import ai.salesfox.portal.database.organization.account.invite.OrganizationAccountInviteTokenRepository;
import ai.salesfox.portal.rest.api.organization.invitation.model.OrganizationAccountInvitationModel;
import ai.salesfox.portal.rest.api.organization.invitation.model.OrganizationAssignableRolesModel;
import ai.salesfox.portal.rest.api.password.PasswordService;
import ai.salesfox.portal.rest.api.registration.organization.model.OrganizationAccountUserRegistrationModel;
import ai.salesfox.portal.rest.api.registration.user.UserRegistrationModel;
import ai.salesfox.portal.rest.api.registration.user.UserRegistrationService;
import ai.salesfox.portal.rest.api.user.profile.UserProfileService;
import ai.salesfox.portal.rest.api.user.profile.model.UserProfileUpdateModel;
import ai.salesfox.portal.rest.api.user.role.UserRoleService;
import ai.salesfox.portal.rest.api.user.role.model.UserRoleModel;
import ai.salesfox.portal.rest.api.user.role.model.UserRoleUpdateModel;
import ai.salesfox.portal.rest.security.authentication.user.PortalUserDetails;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrganizationInvitationService {
    public static final Duration DURATION_OF_TOKEN_VALIDITY = Duration.ofDays(7);

    private final PortalConfiguration portalConfiguration;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationAccountRepository organizationAccountRepository;
    private final OrganizationAccountInviteTokenRepository organizationAccountInviteTokenRepository;
    private final UserRegistrationService userRegistrationService;
    private final UserProfileService userProfileService;
    private final PasswordService passwordService;
    private final UserRoleService userRoleService;
    private final HttpSafeUserMembershipRetrievalService userMembershipRetrievalService;
    private final EmailMessagingService emailMessagingService;

    @Autowired
    public OrganizationInvitationService(
            PortalConfiguration portalConfiguration,
            UserRepository userRepository,
            RoleRepository roleRepository,
            OrganizationAccountRepository organizationAccountRepository,
            OrganizationAccountInviteTokenRepository organizationAccountInviteTokenRepository,
            UserRegistrationService userRegistrationService,
            UserProfileService userProfileService,
            PasswordService passwordService,
            UserRoleService userRoleService,
            HttpSafeUserMembershipRetrievalService userMembershipRetrievalService,
            EmailMessagingService emailMessagingService
    ) {
        this.portalConfiguration = portalConfiguration;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.organizationAccountRepository = organizationAccountRepository;
        this.organizationAccountInviteTokenRepository = organizationAccountInviteTokenRepository;
        this.userRegistrationService = userRegistrationService;
        this.userProfileService = userProfileService;
        this.passwordService = passwordService;
        this.userRoleService = userRoleService;
        this.userMembershipRetrievalService = userMembershipRetrievalService;
        this.emailMessagingService = emailMessagingService;
    }

    public OrganizationAssignableRolesModel getAssignableRoles() {
        List<UserRoleModel> assignableRoles = getAssignableRoleModels();
        return new OrganizationAssignableRolesModel(assignableRoles);
    }

    @Transactional
    public void sendOrganizationAccountInvitation(OrganizationAccountInvitationModel requestModel) {
        validateInviteRequestModel(requestModel);

        UserEntity authenticatedUser = userMembershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity membership = authenticatedUser.getMembershipEntity();
        OrganizationAccountEntity orgAccountEntity = membership.getOrganizationAccountEntity();

        String invitationToken = UUID.randomUUID().toString();
        OrganizationAccountInviteTokenEntity inviteEntity = new OrganizationAccountInviteTokenEntity(requestModel.getInviteEmail(), invitationToken, orgAccountEntity.getOrganizationAccountId(), requestModel.getInviteRole(), PortalDateTimeUtils.getCurrentDateTime());
        organizationAccountInviteTokenRepository.save(inviteEntity);

        sendInvitationEmail(requestModel.getInviteEmail(), orgAccountEntity.getOrganizationAccountName(), invitationToken);
    }

    @Transactional
    public void validateOrganizationAccountInvitation(HttpServletResponse response, String email, String token) {
        if (StringUtils.isBlank(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The parameter 'email' cannot be blank");
        }

        if (StringUtils.isBlank(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The parameter 'token' cannot be blank");
        }

        OrganizationAccountInviteTokenPK invitePK = new OrganizationAccountInviteTokenPK(email, token);
        OrganizationAccountInviteTokenEntity inviteTokenEntity = organizationAccountInviteTokenRepository.findById(invitePK)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

        Duration timeSinceTokenGenerated = Duration.between(inviteTokenEntity.getDateGenerated(), PortalDateTimeUtils.getCurrentDateTime());
        if (timeSinceTokenGenerated.compareTo(DURATION_OF_TOKEN_VALIDITY) < 0) {
            UserEntity tempUser = createUserAccountWithOrgAccountCreationRole(inviteTokenEntity);
            createUserSessionWithOrgAccountCreationPermission(tempUser);

            String frontEndLocation = String.format("%s%s", portalConfiguration.getPortalFrontEndUrl(), portalConfiguration.getFrontEndOrgAcctInviteRoute());
            response.setHeader("Location", frontEndLocation);
            response.setStatus(HttpStatus.FOUND.value());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your invitation has expired. Please contact your organization's account owner.");
        }
    }

    @Transactional
    public void completeOrganizationAccountRegistrationFromInvite(HttpServletResponse httpServletResponse, OrganizationAccountUserRegistrationModel registrationModel) {
        UserEntity temporarilyAuthenticatedUser = userMembershipRetrievalService.getAuthenticatedUserEntity();
        OrganizationAccountInviteTokenEntity inviteTokenEntity = getMostRecentInvitationTokenForUser(temporarilyAuthenticatedUser.getEmail());

        UserProfileUpdateModel userProfileUpdateModel = new UserProfileUpdateModel(
                registrationModel.getFirstName(), registrationModel.getLastName(), temporarilyAuthenticatedUser.getEmail(), registrationModel.getUserAddress(), registrationModel.getMobilePhoneNumber(), registrationModel.getBusinessPhoneNumber());
        userProfileService.updateProfile(temporarilyAuthenticatedUser.getUserId(), userProfileUpdateModel);

        boolean didUpdatePassword = passwordService.updatePassword(temporarilyAuthenticatedUser.getEmail(), registrationModel.getPassword());
        if (!didUpdatePassword) {
            log.error("Could not update password for user [{}] while completing organization account registration", temporarilyAuthenticatedUser.getEmail());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        UserRoleUpdateModel roleUpdateModel = new UserRoleUpdateModel(inviteTokenEntity.getRoleLevel());
        userRoleService.updateRole(temporarilyAuthenticatedUser.getUserId(), roleUpdateModel);

        // Now that the user is fully registered, clear all invitations.
        organizationAccountInviteTokenRepository.deleteByEmail(temporarilyAuthenticatedUser.getEmail());

        clearCreateOrgAccountAuthorityFromSecurityContext();
        httpServletResponse.setHeader("Location", "/");
    }

    private List<UserRoleModel> getAssignableRoleModels() {
        return roleRepository.findByRoleLevelStartingWith(PortalAuthorityConstants.ORGANIZATION_ROLE_PREFIX)
                .stream()
                .filter(role -> !role.getIsRoleRestricted())
                .filter(role -> !PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER.equals(role.getRoleLevel()))
                .map(role -> new UserRoleModel(role.getRoleLevel(), role.getDescription()))
                .collect(Collectors.toList());
    }

    private void validateInviteRequestModel(OrganizationAccountInvitationModel requestModel) {
        Set<String> errors = new LinkedHashSet<>();
        if (StringUtils.isBlank(requestModel.getInviteEmail())) {
            errors.add("Invite Email");
        }

        if (StringUtils.isBlank(requestModel.getInviteRole())) {
            errors.add("Invite Role");
        }

        if (!errors.isEmpty()) {
            String combinedErrors = String.join(", ", errors);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The following fields cannot be blank: %s", combinedErrors));
        }

        if (!FieldValidationUtils.isValidEmailAddress(requestModel.getInviteEmail(), false)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The Invite Email is in an invalid format");
        }

        boolean isInviteRoleAssignable = getAssignableRoleModels()
                .stream()
                .map(UserRoleModel::getLevel)
                .anyMatch(requestModel.getInviteRole()::equals);
        if (!isInviteRoleAssignable) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The Invite Role is invalid");
        }
    }

    private UserEntity createUserAccountWithOrgAccountCreationRole(OrganizationAccountInviteTokenEntity inviteTokenEntity) {
        String invitedEmail = inviteTokenEntity.getEmail();
        OrganizationAccountEntity orgAccount = organizationAccountRepository.findById(inviteTokenEntity.getOrganizationAccountId())
                .orElseThrow(() -> {
                    log.error("Expected organization account with id [{}] to exist in the database", inviteTokenEntity.getOrganizationAccountId());
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });

        String randomTemporaryPassword = UUID.randomUUID().toString();
        UserRegistrationModel userRegistrationModel = new UserRegistrationModel("First Name", "Last Name", invitedEmail, randomTemporaryPassword, PortalAuthorityConstants.CREATE_ORGANIZATION_ACCOUNT_PERMISSION);

        return findExistingTempUser(invitedEmail)
                .orElseGet(() -> userRegistrationService.registerOrganizationUser(userRegistrationModel, orgAccount));
    }

    private void createUserSessionWithOrgAccountCreationPermission(UserEntity user) {
        String temporaryRoleName = PortalAuthorityConstants.CREATE_ORGANIZATION_ACCOUNT_PERMISSION;
        PortalUserDetails userDetails = new PortalUserDetails(List.of(temporaryRoleName), user.getEmail(), null, false, true);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, List.of(new SimpleGrantedAuthority(temporaryRoleName)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private OrganizationAccountInviteTokenEntity getMostRecentInvitationTokenForUser(String email) {
        return organizationAccountInviteTokenRepository.findByEmail(email)
                .stream()
                .max(Comparator.comparing(OrganizationAccountInviteTokenEntity::getDateGenerated))
                .orElseThrow(() -> {
                    log.warn("An attempt to complete organization account registration was made, but no invitation exists for the authenticated user wit email [{}]", email);
                    return new ResponseStatusException(HttpStatus.FORBIDDEN);
                });
    }

    private Optional<UserEntity> findExistingTempUser(String emailAddress) {
        return userRepository.findFirstByEmail(emailAddress)
                .filter(user -> user
                        .getMembershipEntity()
                        .getRoleEntity()
                        .getRoleLevel()
                        .equals(PortalAuthorityConstants.CREATE_ORGANIZATION_ACCOUNT_PERMISSION)
                );
    }

    private void clearCreateOrgAccountAuthorityFromSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void sendInvitationEmail(String email, String organizationAccountName, String invitationToken) {
        String invitationUrl = createInvitationLink(email, invitationToken);

        log.info("*** REMOVE ME *** Invitation Link: {}", invitationUrl);
        EmailMessageModel emailMessage = createInvitationMessageModel(email, organizationAccountName, invitationUrl);
        try {
            emailMessagingService.sendMessage(emailMessage);
        } catch (PortalEmailException e) {
            log.error("Problem sending organization account invitation email", e);
        }
    }

    private ButtonEmailMessageModel createInvitationMessageModel(String recipientEmail, String organizationAccountName, String invitationUrl) {
        return new ButtonEmailMessageModel(
                List.of(recipientEmail),
                String.format("Invitation to join %s on Salesfox", organizationAccountName),
                "Invitation",
                String.format("You have been invited to join the '%s' account on Salesfox. Click the button below to accept.", organizationAccountName),
                "Accept Invitation",
                invitationUrl
        );
    }

    private String createInvitationLink(String email, String invitationToken) {
        StringBuilder linkBuilder = new StringBuilder(portalConfiguration.getPortalBackEndUrl());
        linkBuilder.append(OrganizationInvitationController.VALIDATE_INVITE_ENDPOINT);
        linkBuilder.append("?token=");
        linkBuilder.append(invitationToken);
        linkBuilder.append("&email=");
        linkBuilder.append(email);
        return linkBuilder.toString();
    }

}
