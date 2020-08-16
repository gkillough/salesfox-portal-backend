package com.getboostr.portal.rest.api.organization.invitation;

import com.getboostr.portal.PortalConfiguration;
import com.getboostr.portal.common.FieldValidationUtils;
import com.getboostr.portal.common.exception.PortalException;
import com.getboostr.portal.common.service.email.EmailMessagingService;
import com.getboostr.portal.common.service.email.PortalEmailException;
import com.getboostr.portal.common.service.email.model.ButtonEmailMessageModel;
import com.getboostr.portal.common.service.email.model.EmailMessageModel;
import com.getboostr.portal.common.service.license.LicenseSeatManager;
import com.getboostr.portal.common.time.PortalDateTimeUtils;
import com.getboostr.portal.database.account.entity.LicenseEntity;
import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.account.repository.RoleRepository;
import com.getboostr.portal.database.organization.account.OrganizationAccountEntity;
import com.getboostr.portal.database.organization.account.OrganizationAccountRepository;
import com.getboostr.portal.database.organization.account.invite.OrganizationAccountInviteTokenEntity;
import com.getboostr.portal.database.organization.account.invite.OrganizationAccountInviteTokenPK;
import com.getboostr.portal.database.organization.account.invite.OrganizationAccountInviteTokenRepository;
import com.getboostr.portal.rest.api.organization.invitation.model.OrganizationAccountInvitationModel;
import com.getboostr.portal.rest.api.organization.invitation.model.OrganizationAssignableRolesModel;
import com.getboostr.portal.rest.api.password.PasswordService;
import com.getboostr.portal.rest.api.registration.RegistrationController;
import com.getboostr.portal.rest.api.registration.organization.model.OrganizationAccountUserRegistrationModel;
import com.getboostr.portal.rest.api.registration.user.UserRegistrationModel;
import com.getboostr.portal.rest.api.registration.user.UserRegistrationService;
import com.getboostr.portal.rest.api.user.profile.UserProfileService;
import com.getboostr.portal.rest.api.user.profile.model.UserProfileUpdateModel;
import com.getboostr.portal.rest.api.user.role.UserRoleService;
import com.getboostr.portal.rest.api.user.role.model.UserRoleModel;
import com.getboostr.portal.rest.api.user.role.model.UserRoleUpdateModel;
import com.getboostr.portal.rest.security.authentication.user.PortalUserDetails;
import com.getboostr.portal.rest.security.authorization.PortalAuthorityConstants;
import com.getboostr.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
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
    private final RoleRepository roleRepository;
    private final OrganizationAccountRepository organizationAccountRepository;
    private final OrganizationAccountInviteTokenRepository organizationAccountInviteTokenRepository;
    private final UserRegistrationService userRegistrationService;
    private final UserDetailsService userDetailsService;
    private final UserProfileService userProfileService;
    private final PasswordService passwordService;
    private final UserRoleService userRoleService;
    private final LicenseSeatManager licenseSeatManager;
    private final HttpSafeUserMembershipRetrievalService userMembershipRetrievalService;
    private final EmailMessagingService emailMessagingService;

    @Autowired
    public OrganizationInvitationService(PortalConfiguration portalConfiguration, RoleRepository roleRepository, OrganizationAccountRepository organizationAccountRepository, OrganizationAccountInviteTokenRepository organizationAccountInviteTokenRepository,
                                         UserRegistrationService userRegistrationService, UserDetailsService userDetailsService, UserProfileService userProfileService, PasswordService passwordService, UserRoleService userRoleService,
                                         LicenseSeatManager licenseSeatManager, HttpSafeUserMembershipRetrievalService userMembershipRetrievalService, EmailMessagingService emailMessagingService) {
        this.portalConfiguration = portalConfiguration;
        this.roleRepository = roleRepository;
        this.organizationAccountRepository = organizationAccountRepository;
        this.organizationAccountInviteTokenRepository = organizationAccountInviteTokenRepository;
        this.userRegistrationService = userRegistrationService;
        this.userDetailsService = userDetailsService;
        this.userProfileService = userProfileService;
        this.passwordService = passwordService;
        this.userRoleService = userRoleService;
        this.licenseSeatManager = licenseSeatManager;
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

        if (!orgAccountEntity.getOrganizationAccountName().equals(requestModel.getOrganizationAccountName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You do not have access to that Organization Account");
        }

        try {
            LicenseEntity orgLicense = licenseSeatManager.getLicenseForOrganizationAccount(orgAccountEntity);
            if (!licenseSeatManager.hasAvailableSeats(orgLicense)) {
                throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "No available license seats");
            }
        } catch (PortalException e) {
            log.error("There was a problem managing the organization license", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

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

        try {
            LicenseEntity orgLicense = licenseSeatManager.getLicenseForOrganizationAccountId(inviteTokenEntity.getOrganizationAccountId());
            if (!licenseSeatManager.hasAvailableSeats(orgLicense)) {
                throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "No available license seats");
            }
        } catch (PortalException e) {
            log.error("There was a problem managing the organization license", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Duration timeSinceTokenGenerated = Duration.between(inviteTokenEntity.getDateGenerated(), PortalDateTimeUtils.getCurrentDateTime());
        if (timeSinceTokenGenerated.compareTo(DURATION_OF_TOKEN_VALIDITY) < 0) {
            UserEntity tempUser = createUserAccountWithOrgAccountCreationRole(inviteTokenEntity);
            createUserSessionWithOrgAccountCreationPermission(tempUser);
            response.setHeader("Location", RegistrationController.BASE_ENDPOINT + RegistrationController.ORGANIZATION_ACCOUNT_USER_ENDPOINT_SUFFIX);
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
        if (StringUtils.isBlank(requestModel.getOrganizationAccountName())) {
            errors.add("Organization Account Name");
        }

        if (StringUtils.isBlank(requestModel.getInviteEmail())) {
            errors.add("Invite Email");
        }

        if (StringUtils.isBlank(requestModel.getOrganizationAccountName())) {
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
        OrganizationAccountEntity orgAccount = organizationAccountRepository.findById(inviteTokenEntity.getOrganizationAccountId())
                .orElseThrow(() -> {
                    log.error("Expected organization account with id [{}] to exist in the database", inviteTokenEntity.getOrganizationAccountId());
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });

        String randomTemporaryPassword = UUID.randomUUID().toString();
        UserRegistrationModel userRegistrationModel = new UserRegistrationModel("First Name", "Last Name", inviteTokenEntity.getEmail(), randomTemporaryPassword, PortalAuthorityConstants.CREATE_ORGANIZATION_ACCOUNT_PERMISSION);
        return userRegistrationService.registerOrganizationUser(userRegistrationModel, orgAccount);
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
                String.format("Invitation to join %s on BOOSTR", organizationAccountName),
                "Invitation",
                String.format("You have been invited to join the '%s' account on BOOSTR. Click the button below to accept.", organizationAccountName),
                "Accept Invitation",
                invitationUrl
        );
    }

    private String createInvitationLink(String email, String invitationToken) {
        StringBuilder linkBuilder = new StringBuilder(portalConfiguration.getPortalBaseUrl());
        linkBuilder.append(portalConfiguration.getInviteOrganizationAccountUserLinkSpec());
        linkBuilder.append("?token=");
        linkBuilder.append(invitationToken);
        linkBuilder.append("&email=");
        linkBuilder.append(email);
        return linkBuilder.toString();
    }

}
