package com.usepipeline.portal.web.organization.invitation;

import com.usepipeline.portal.common.FieldValidationUtils;
import com.usepipeline.portal.common.service.email.EmailMessage;
import com.usepipeline.portal.common.service.email.EmailMessagingService;
import com.usepipeline.portal.common.service.email.PortalEmailException;
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
import com.usepipeline.portal.web.registration.RegistrationController;
import com.usepipeline.portal.web.registration.user.UserRegistrationModel;
import com.usepipeline.portal.web.registration.user.UserRegistrationService;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import com.usepipeline.portal.web.user.role.model.UserRoleModel;
import com.usepipeline.portal.web.util.HttpSafeUserMembershipRetrievalService;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrganizationInvitationService {
    public static final Duration DURATION_OF_TOKEN_VALIDITY = Duration.ofDays(7);

    private RoleRepository roleRepository;
    private OrganizationAccountRepository organizationAccountRepository;
    private OrganizationAccountInviteTokenRepository organizationAccountInviteTokenRepository;
    private UserRegistrationService userRegistrationService;
    private HttpSafeUserMembershipRetrievalService userMembershipRetrievalService;
    private EmailMessagingService emailMessagingService;

    @Autowired
    public OrganizationInvitationService(RoleRepository roleRepository, OrganizationAccountRepository organizationAccountRepository, OrganizationAccountInviteTokenRepository organizationAccountInviteTokenRepository,
                                         UserRegistrationService userRegistrationService, HttpSafeUserMembershipRetrievalService userMembershipRetrievalService, EmailMessagingService emailMessagingService) {
        this.roleRepository = roleRepository;
        this.organizationAccountRepository = organizationAccountRepository;
        this.organizationAccountInviteTokenRepository = organizationAccountInviteTokenRepository;
        this.userRegistrationService = userRegistrationService;
        this.userMembershipRetrievalService = userMembershipRetrievalService;
        this.emailMessagingService = emailMessagingService;
    }

    public OrganizationAssignableRolesModel getAssignableRoles() {
        List<UserRoleModel> assignableRoles = getAssignableRoleModels();
        return new OrganizationAssignableRolesModel(assignableRoles);
    }

    @Transactional
    public void sendOrganizationAccountInvitation(OrganizationAccountInvitationModel requestModel) {
        validateRequestModel(requestModel);

        UserEntity authenticatedUser = userMembershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity membership = userMembershipRetrievalService.getMembershipEntity(authenticatedUser);
        OrganizationAccountEntity orgAccountEntity = userMembershipRetrievalService.getOrganizationAccountEntity(membership);

        if (!orgAccountEntity.getOrganizationAccountName().equals(requestModel.getOrganizationAccountName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You do not have access to that Organization Account");
        }

        String invitationToken = UUID.randomUUID().toString();
        OrganizationAccountInviteTokenEntity inviteEntity = new OrganizationAccountInviteTokenEntity(requestModel.getInviteEmail(), invitationToken, orgAccountEntity.getOrganizationAccountId(), requestModel.getInviteRole(), LocalDateTime.now());
        organizationAccountInviteTokenRepository.save(inviteEntity);

        sendInvitationEmail(requestModel.getInviteEmail(), invitationToken);
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

        Duration timeSinceTokenGenerated = Duration.between(inviteTokenEntity.getDateGenerated(), LocalDateTime.now());
        if (timeSinceTokenGenerated.compareTo(DURATION_OF_TOKEN_VALIDITY) < 0) {
            createUserAccountWithOrgAccountCreationRole(inviteTokenEntity);
            createUserSessionWithOrgAccountCreationPermssion(email);
            response.setHeader("Location", RegistrationController.BASE_ENDPOINT + RegistrationController.ORGANIZATION_ACCOUNT_USER_ENDPOINT_SUFFIX);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your invitation has expired. Please contact your organization's account owner.");
        }
    }

    private List<UserRoleModel> getAssignableRoleModels() {
        // TODO determine if we should allow multiple account owners
        return roleRepository.findByRoleLevelStartingWith(PortalAuthorityConstants.ORGANIZATION_ROLE_PREFIX)
                .stream()
                .filter(role -> !role.getIsRoleRestricted())
                .map(role -> new UserRoleModel(role.getRoleLevel(), role.getDescription()))
                .collect(Collectors.toList());
    }

    private void validateRequestModel(OrganizationAccountInvitationModel requestModel) {
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
        UserRegistrationModel userRegistrationModel = new UserRegistrationModel(StringUtils.EMPTY, StringUtils.EMPTY, inviteTokenEntity.getEmail(), randomTemporaryPassword, orgAccount.getOrganizationAccountName());
        userRegistrationService.registerOrganizationUser(userRegistrationModel, orgAccount.getOrganizationId(), PortalAuthorityConstants.CREATE_ORGANIZATION_ACCOUNT_PERMISSION);
    }

    private void createUserSessionWithOrgAccountCreationPermssion(String email) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                email, null, Collections.singletonList(new SimpleGrantedAuthority(PortalAuthorityConstants.CREATE_ORGANIZATION_ACCOUNT_PERMISSION)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void sendInvitationEmail(String email, String invitationToken) {
        // TODO implement
        String url = String.format("localhost:8080/api/organization/invite/validate?token=%s&email=%s", invitationToken, email);
        log.info("*** REMOVE ME *** Invitation Link: {}", url);

        // TODO create email message
        EmailMessage emailMessage = null;

        try {
            emailMessagingService.sendMessage(emailMessage);
        } catch (PortalEmailException e) {
            log.error("Problem sending organization account invitation email", e);
        }
    }

}
