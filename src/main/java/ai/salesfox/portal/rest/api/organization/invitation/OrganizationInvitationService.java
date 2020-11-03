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
import ai.salesfox.portal.database.organization.OrganizationEntity;
import ai.salesfox.portal.database.organization.OrganizationRepository;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountRepository;
import ai.salesfox.portal.database.organization.account.invite.OrganizationAccountInviteTokenEntity;
import ai.salesfox.portal.database.organization.account.invite.OrganizationAccountInviteTokenPK;
import ai.salesfox.portal.database.organization.account.invite.OrganizationAccountInviteTokenRepository;
import ai.salesfox.portal.rest.api.common.model.response.ValidationResponseModel;
import ai.salesfox.portal.rest.api.organization.invitation.model.OrganizationAccountInvitationModel;
import ai.salesfox.portal.rest.api.organization.invitation.model.OrganizationAssignableRolesModel;
import ai.salesfox.portal.rest.api.registration.organization.model.OrganizationAccountUserRegistrationModel;
import ai.salesfox.portal.rest.api.registration.user.UserRegistrationModel;
import ai.salesfox.portal.rest.api.registration.user.UserRegistrationService;
import ai.salesfox.portal.rest.api.user.profile.UserProfileService;
import ai.salesfox.portal.rest.api.user.profile.model.UserProfileUpdateModel;
import ai.salesfox.portal.rest.api.user.role.model.UserRoleModel;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrganizationInvitationService {
    public static final Duration DURATION_OF_TOKEN_VALIDITY = Duration.ofDays(7);

    private final PortalConfiguration portalConfiguration;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationAccountRepository organizationAccountRepository;
    private final OrganizationAccountInviteTokenRepository organizationAccountInviteTokenRepository;
    private final UserRegistrationService userRegistrationService;
    private final UserProfileService userProfileService;
    private final HttpSafeUserMembershipRetrievalService userMembershipRetrievalService;
    private final EmailMessagingService emailMessagingService;

    @Autowired
    public OrganizationInvitationService(
            PortalConfiguration portalConfiguration,
            RoleRepository roleRepository,
            OrganizationRepository organizationRepository,
            OrganizationAccountRepository organizationAccountRepository,
            OrganizationAccountInviteTokenRepository organizationAccountInviteTokenRepository,
            UserRegistrationService userRegistrationService,
            UserProfileService userProfileService,
            HttpSafeUserMembershipRetrievalService userMembershipRetrievalService,
            EmailMessagingService emailMessagingService
    ) {
        this.portalConfiguration = portalConfiguration;
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
        this.organizationAccountRepository = organizationAccountRepository;
        this.organizationAccountInviteTokenRepository = organizationAccountInviteTokenRepository;
        this.userRegistrationService = userRegistrationService;
        this.userProfileService = userProfileService;
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
        OrganizationEntity orgEntity = organizationRepository.findById(orgAccountEntity.getOrganizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Organization missing!"));

        String invitationToken = UUID.randomUUID().toString();
        OrganizationAccountInviteTokenEntity inviteEntity = new OrganizationAccountInviteTokenEntity(requestModel.getInviteEmail(), invitationToken, orgAccountEntity.getOrganizationAccountId(), requestModel.getInviteRole(), PortalDateTimeUtils.getCurrentDateTime());
        organizationAccountInviteTokenRepository.save(inviteEntity);

        sendInvitationEmail(requestModel.getInviteEmail(), orgEntity.getOrganizationName(), orgAccountEntity.getOrganizationAccountName(), invitationToken);
    }

    @Transactional(readOnly = true)
    public ValidationResponseModel validateOrganizationAccountInvitation(String email, String token) {
        OrganizationAccountInviteTokenEntity inviteTokenEntity = validateAndRetrieveInviteEntity(email, token);
        Duration timeSinceTokenGenerated = Duration.between(inviteTokenEntity.getDateGenerated(), PortalDateTimeUtils.getCurrentDateTime());
        if (timeSinceTokenGenerated.compareTo(DURATION_OF_TOKEN_VALIDITY) < 0) {
            Duration durationOfRemainingTokenValidity = DURATION_OF_TOKEN_VALIDITY.minus(timeSinceTokenGenerated);
            Long remainingDays = durationOfRemainingTokenValidity.toDaysPart();
            int remainingHours = durationOfRemainingTokenValidity.toHoursPart();
            int remainingMinutes = durationOfRemainingTokenValidity.toMinutesPart();
            return new ValidationResponseModel(true, String.format("This invitation expires in %s days, %d hours, %d minutes", remainingDays.toString(), remainingHours, remainingMinutes));
        } else {
            return ValidationResponseModel.invalid("Your invitation has expired. Please contact your organization's account owner.");
        }
    }

    @Transactional
    public void completeOrganizationAccountRegistrationFromInvite(OrganizationAccountUserRegistrationModel registrationModel) {
        String email = registrationModel.getEmail();
        String token = registrationModel.getToken();
        OrganizationAccountInviteTokenEntity inviteTokenEntity = validateAndRetrieveInviteEntity(email, token);

        OrganizationAccountEntity orgAccount = organizationAccountRepository.findById(inviteTokenEntity.getOrganizationAccountId())
                .filter(OrganizationAccountEntity::getIsActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "The organization account does not exist"));

        UserRegistrationModel userRegistrationModel = new UserRegistrationModel(registrationModel.getFirstName(), registrationModel.getLastName(), email, registrationModel.getPassword(), inviteTokenEntity.getRoleLevel());
        UserEntity newUser = userRegistrationService.registerOrganizationUser(userRegistrationModel, orgAccount);

        UserProfileUpdateModel userProfileUpdateModel = new UserProfileUpdateModel(
                registrationModel.getFirstName(), registrationModel.getLastName(), email, registrationModel.getUserAddress(), registrationModel.getMobilePhoneNumber(), registrationModel.getBusinessPhoneNumber());
        userProfileService.updateProfileWithoutPermissionsCheck(newUser.getUserId(), userProfileUpdateModel);

        // Now that the user is fully registered, clear all invitations.
        organizationAccountInviteTokenRepository.deleteByEmail(email);
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

    private OrganizationAccountInviteTokenEntity validateAndRetrieveInviteEntity(String email, String token) {
        if (StringUtils.isBlank(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The parameter 'email' cannot be blank");
        }

        if (StringUtils.isBlank(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The parameter 'token' cannot be blank");
        }

        OrganizationAccountInviteTokenPK invitePK = new OrganizationAccountInviteTokenPK(email, token);
        return organizationAccountInviteTokenRepository.findById(invitePK)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No invitation exists for that email/token combination"));
    }

    private void sendInvitationEmail(String email, String organizationName, String organizationAccountName, String invitationToken) {
        String invitationUrl = createInvitationLink(email, invitationToken);

        log.info("*** REMOVE ME *** Invitation Link: {}", invitationUrl);
        EmailMessageModel emailMessage = createInvitationMessageModel(email, organizationName, organizationAccountName, invitationUrl);
        try {
            emailMessagingService.sendMessage(emailMessage);
        } catch (PortalEmailException e) {
            log.error("Problem sending organization account invitation email", e);
        }
    }

    private ButtonEmailMessageModel createInvitationMessageModel(String recipientEmail, String organizationName, String organizationAccountName, String invitationUrl) {
        return new ButtonEmailMessageModel(
                List.of(recipientEmail),
                String.format("Invitation to join '%s - %s' on Salesfox", organizationName, organizationAccountName),
                "Invitation",
                String.format("You have been invited to join the '%s - %s' account on Salesfox. Click the button below to accept.", organizationName, organizationAccountName),
                "Accept Invitation",
                invitationUrl
        );
    }

    private String createInvitationLink(String email, String invitationToken) {
        StringBuilder linkBuilder = new StringBuilder(portalConfiguration.getPortalFrontEndUrl());
        linkBuilder.append(portalConfiguration.getFrontEndOrgAcctInviteRoute());
        linkBuilder.append("?token=");
        linkBuilder.append(invitationToken);
        linkBuilder.append("&email=");
        linkBuilder.append(email);
        return linkBuilder.toString();
    }

}
