package com.usepipeline.portal.web.organization.invitation;

import com.usepipeline.portal.common.FieldValidationUtils;
import com.usepipeline.portal.common.service.email.EmailMessage;
import com.usepipeline.portal.common.service.email.EmailMessagingService;
import com.usepipeline.portal.common.service.email.PortalEmailException;
import com.usepipeline.portal.database.account.entity.MembershipEntity;
import com.usepipeline.portal.database.account.repository.MembershipRepository;
import com.usepipeline.portal.database.account.repository.RoleRepository;
import com.usepipeline.portal.database.account.repository.UserRepository;
import com.usepipeline.portal.database.organization.account.OrganizationAccountEntity;
import com.usepipeline.portal.database.organization.account.OrganizationAccountRepository;
import com.usepipeline.portal.database.organization.account.invite.OrganizationAccountInviteTokenEntity;
import com.usepipeline.portal.database.organization.account.invite.OrganizationAccountInviteTokenRepository;
import com.usepipeline.portal.web.organization.invitation.model.OrganizationAccountInvitationModel;
import com.usepipeline.portal.web.organization.invitation.model.OrganizationAssignableRolesModel;
import com.usepipeline.portal.web.security.authentication.SecurityContextUtils;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import com.usepipeline.portal.web.user.role.model.UserRoleModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrganizationInvitationService {
    private RoleRepository roleRepository;
    private UserRepository userRepository;
    private MembershipRepository membershipRepository;
    private OrganizationAccountRepository organizationAccountRepository;
    private OrganizationAccountInviteTokenRepository organizationAccountInviteTokenRepository;
    private EmailMessagingService emailMessagingService;

    @Autowired
    public OrganizationInvitationService(RoleRepository roleRepository, UserRepository userRepository, MembershipRepository membershipRepository,
                                         OrganizationAccountRepository organizationAccountRepository, OrganizationAccountInviteTokenRepository organizationAccountInviteTokenRepository,
                                         EmailMessagingService emailMessagingService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.organizationAccountRepository = organizationAccountRepository;
        this.organizationAccountInviteTokenRepository = organizationAccountInviteTokenRepository;
        this.emailMessagingService = emailMessagingService;
    }

    public OrganizationAssignableRolesModel getAssignableRoles() {
        List<UserRoleModel> assignableRoles = getAssignableRoleModels();
        return new OrganizationAssignableRolesModel(assignableRoles);
    }

    @Transactional
    public void sendOrganizationAccountInvitation(OrganizationAccountInvitationModel requestModel) {
        validateRequestModel(requestModel);
        Long organizationAccountId = getLoggedInOrgOwnerOrgAccountId();
        OrganizationAccountEntity orgAccountEntity = getOrgAccountEntity(organizationAccountId);
        if (!orgAccountEntity.getOrganizationAccountName().equals(requestModel.getOrganizationAccountName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You do not have access to that Organization Account");
        }

        String invitationToken = UUID.randomUUID().toString();
        OrganizationAccountInviteTokenEntity inviteEntity = new OrganizationAccountInviteTokenEntity(requestModel.getInviteEmail(), invitationToken, organizationAccountId, requestModel.getInviteRole(), LocalDateTime.now());
        organizationAccountInviteTokenRepository.save(inviteEntity);

        sendInvitationEmail(requestModel.getInviteEmail(), invitationToken);
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

    private Long getLoggedInOrgOwnerOrgAccountId() {
        UserDetails loggedInUser = getLoggedInUser();
        return userRepository.findFirstByEmail(loggedInUser.getUsername())
                .flatMap(user -> membershipRepository.findFirstByUserId(user.getUserId()))
                .map(MembershipEntity::getOrganizationAccountId)
                .orElseThrow(() -> {
                    log.error("User missing from database: {}" + loggedInUser.getUsername());
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    private UserDetails getLoggedInUser() {
        return SecurityContextUtils.retrieveUserAuthToken()
                .map(SecurityContextUtils::extractUserDetails)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private OrganizationAccountEntity getOrgAccountEntity(Long orgAccountId) {
        return organizationAccountRepository.findById(orgAccountId)
                .orElseThrow(() -> {
                    log.error("Organization Account with id [{}] missing from the database", orgAccountId);
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
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
