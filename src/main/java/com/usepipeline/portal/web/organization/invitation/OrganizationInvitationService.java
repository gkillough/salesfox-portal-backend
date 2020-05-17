package com.usepipeline.portal.web.organization.invitation;

import com.usepipeline.portal.PortalConfiguration;
import com.usepipeline.portal.common.FieldValidationUtils;
import com.usepipeline.portal.common.exception.PortalException;
import com.usepipeline.portal.common.service.email.EmailMessagingService;
import com.usepipeline.portal.common.service.email.PortalEmailException;
import com.usepipeline.portal.common.service.email.model.EmailMessageModel;
import com.usepipeline.portal.common.service.license.LicenseSeatManager;
import com.usepipeline.portal.database.account.entity.LicenseEntity;
import com.usepipeline.portal.database.account.entity.MembershipEntity;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.account.repository.RoleRepository;
import com.usepipeline.portal.database.organization.account.OrganizationAccountEntity;
import com.usepipeline.portal.database.organization.account.OrganizationAccountRepository;
import com.usepipeline.portal.database.organization.account.invite.OrganizationAccountInviteTokenEntity;
import com.usepipeline.portal.database.organization.account.invite.OrganizationAccountInviteTokenPK;
import com.usepipeline.portal.database.organization.account.invite.OrganizationAccountInviteTokenRepository;
import com.usepipeline.portal.web.organization.invitation.model.OrganizationAccountInvitationModel;
import com.usepipeline.portal.web.organization.invitation.model.OrganizationAssignableRolesModel;
import com.usepipeline.portal.web.password.PasswordService;
import com.usepipeline.portal.web.registration.RegistrationController;
import com.usepipeline.portal.web.registration.organization.model.OrganizationAccountUserRegistrationModel;
import com.usepipeline.portal.web.registration.user.UserRegistrationModel;
import com.usepipeline.portal.web.registration.user.UserRegistrationService;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import com.usepipeline.portal.web.user.profile.UserProfileService;
import com.usepipeline.portal.web.user.profile.model.UserProfileUpdateModel;
import com.usepipeline.portal.web.user.role.UserRoleService;
import com.usepipeline.portal.web.user.role.model.UserRoleModel;
import com.usepipeline.portal.web.user.role.model.UserRoleUpdateModel;
import com.usepipeline.portal.web.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrganizationInvitationService {
    public static final Duration DURATION_OF_TOKEN_VALIDITY = Duration.ofDays(7);

    private PortalConfiguration portalConfiguration;
    private RoleRepository roleRepository;
    private OrganizationAccountRepository organizationAccountRepository;
    private OrganizationAccountInviteTokenRepository organizationAccountInviteTokenRepository;
    private UserRegistrationService userRegistrationService;
    private UserDetailsService userDetailsService;
    private UserProfileService userProfileService;
    private PasswordService passwordService;
    private UserRoleService userRoleService;
    private LicenseSeatManager licenseSeatManager;
    private HttpSafeUserMembershipRetrievalService userMembershipRetrievalService;
    private EmailMessagingService emailMessagingService;

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
        MembershipEntity membership = userMembershipRetrievalService.getMembershipEntity(authenticatedUser);
        OrganizationAccountEntity orgAccountEntity = userMembershipRetrievalService.getOrganizationAccountEntity(membership);

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
        OrganizationAccountInviteTokenEntity inviteEntity = new OrganizationAccountInviteTokenEntity(requestModel.getInviteEmail(), invitationToken, orgAccountEntity.getOrganizationAccountId(), requestModel.getInviteRole(), LocalDateTime.now());
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

        Duration timeSinceTokenGenerated = Duration.between(inviteTokenEntity.getDateGenerated(), LocalDateTime.now());
        if (timeSinceTokenGenerated.compareTo(DURATION_OF_TOKEN_VALIDITY) < 0) {
            createUserAccountWithOrgAccountCreationRole(inviteTokenEntity);
            createUserSessionWithOrgAccountCreationPermission(email);
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

    private void createUserAccountWithOrgAccountCreationRole(OrganizationAccountInviteTokenEntity inviteTokenEntity) {
        OrganizationAccountEntity orgAccount = organizationAccountRepository.findById(inviteTokenEntity.getOrganizationAccountId())
                .orElseThrow(() -> {
                    log.error("Expected organization account with id [{}] to exist in the database", inviteTokenEntity.getOrganizationAccountId());
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });

        String randomTemporaryPassword = UUID.randomUUID().toString();
        UserRegistrationModel userRegistrationModel = new UserRegistrationModel("First Name", "Last Name", inviteTokenEntity.getEmail(), randomTemporaryPassword, orgAccount.getOrganizationAccountName());
        userRegistrationService.registerOrganizationUser(userRegistrationModel, orgAccount.getOrganizationId(), PortalAuthorityConstants.CREATE_ORGANIZATION_ACCOUNT_PERMISSION);
    }

    private void createUserSessionWithOrgAccountCreationPermission(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, List.of(new SimpleGrantedAuthority(PortalAuthorityConstants.CREATE_ORGANIZATION_ACCOUNT_PERMISSION)));
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

        String subjectLine = String.format("Invitation to join %s on PIPELINE", organizationAccountName);
        EmailMessageModel emailMessage = new EmailMessageModel(Collections.singletonList(email), subjectLine, invitationUrl);
        try {
            emailMessagingService.sendMessage(emailMessage);
        } catch (PortalEmailException e) {
            log.error("Problem sending organization account invitation email", e);
        }
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
