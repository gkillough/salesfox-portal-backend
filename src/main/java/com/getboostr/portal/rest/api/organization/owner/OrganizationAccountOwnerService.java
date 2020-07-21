package com.getboostr.portal.rest.api.organization.owner;

import com.getboostr.portal.common.enumeration.AccessOperation;
import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.RoleEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.account.repository.MembershipRepository;
import com.getboostr.portal.database.account.repository.RoleRepository;
import com.getboostr.portal.database.account.repository.UserRepository;
import com.getboostr.portal.database.organization.account.OrganizationAccountEntity;
import com.getboostr.portal.rest.api.organization.common.OrganizationAccessService;
import com.getboostr.portal.rest.api.organization.users.model.NewAccountOwnerRequestModel;
import com.getboostr.portal.database.organization.account.OrganizationAccountRepository;
import com.getboostr.portal.rest.security.authorization.PortalAuthorityConstants;
import com.getboostr.portal.rest.api.user.profile.UserProfileService;
import com.getboostr.portal.rest.api.user.profile.model.UserProfileModel;
import com.getboostr.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class OrganizationAccountOwnerService {
    private OrganizationAccountRepository organizationAccountRepository;
    private UserRepository userRepository;
    private MembershipRepository membershipRepository;
    private RoleRepository roleRepository;
    private UserProfileService userProfileService;
    private OrganizationAccessService organizationAccessService;
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public OrganizationAccountOwnerService(OrganizationAccountRepository organizationAccountRepository, UserRepository userRepository, MembershipRepository membershipRepository, RoleRepository roleRepository,
                                           UserProfileService userProfileService, OrganizationAccessService organizationAccessService, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.organizationAccountRepository = organizationAccountRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.roleRepository = roleRepository;
        this.userProfileService = userProfileService;
        this.organizationAccessService = organizationAccessService;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public UserProfileModel getOrganizationAccountOwner(UUID organizationAccountId) {
        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.findById(organizationAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity authenticatedUserEntity = membershipRetrievalService.getAuthenticatedUserEntity();
        validateUserHasAccessToOrgAccount(authenticatedUserEntity, orgAccountEntity, AccessOperation.READ);

        RoleEntity orgAccountOwnerRole = getExistingRole(PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER);
        UserEntity orgAccountOwnerEntity = getOrganizationAccountOwnerEntity(orgAccountEntity, orgAccountOwnerRole);
        return userProfileService.retrieveProfileWithoutPermissionCheck(orgAccountOwnerEntity.getUserId());
    }

    @Transactional
    public void transferOrganizationAccountOwnership(UUID organizationAccountId, NewAccountOwnerRequestModel updateModel) {
        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.findById(organizationAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity authenticatedUserEntity = membershipRetrievalService.getAuthenticatedUserEntity();
        validateUserHasAccessToOrgAccount(authenticatedUserEntity, orgAccountEntity, AccessOperation.UPDATE);

        if (StringUtils.isBlank(updateModel.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The 'email' field cannot be blank");
        }

        UserEntity candidateOrgAcctOwner = userRepository.findFirstByEmail(updateModel.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No user with the email ['%s'] exists", updateModel.getEmail())));

        MembershipEntity candidateOrgAcctOwnerMembership = membershipRetrievalService.getMembershipEntity(candidateOrgAcctOwner);
        if (!candidateOrgAcctOwnerMembership.getOrganizationAccountId().equals(organizationAccountId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The user with the email ['%s'] is not a member of the Organization Account", updateModel.getEmail()));
        }

        RoleEntity roleEntity = membershipRetrievalService.getRoleEntity(candidateOrgAcctOwnerMembership);
        if (!roleEntity.getRoleLevel().startsWith(PortalAuthorityConstants.ORGANIZATION_ROLE_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The user with the email ['%s'] does not have a valid role in the Organization Account", updateModel.getEmail()));
        }

        RoleEntity orgAcctOwnerRole = getExistingRole(PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER);
        RoleEntity orgAcctManagerRole = getExistingRole(PortalAuthorityConstants.ORGANIZATION_ACCOUNT_MANAGER);

        UserEntity currentOrgAcctOwner = getOrganizationAccountOwnerEntity(orgAccountEntity, orgAcctOwnerRole);
        MembershipEntity currentOrgAcctOwnerMembership = membershipRetrievalService.getMembershipEntity(currentOrgAcctOwner);

        candidateOrgAcctOwnerMembership.setRoleId(orgAcctOwnerRole.getRoleId());
        currentOrgAcctOwnerMembership.setRoleId(orgAcctManagerRole.getRoleId());

        List<MembershipEntity> membershipsToSave = List.of(candidateOrgAcctOwnerMembership, currentOrgAcctOwnerMembership);
        membershipRepository.saveAll(membershipsToSave);

        if (authenticatedUserEntity.getUserId().equals(currentOrgAcctOwner.getUserId())) {
            // If the "old" org account owner is the one making the request, his/her session will still have the role until logging out.
            SecurityContextHolder.clearContext();
        }
    }

    private UserEntity getOrganizationAccountOwnerEntity(OrganizationAccountEntity orgAccount, RoleEntity role) {
        List<MembershipEntity> orgAccountOwners = membershipRepository.findByRoleIdAndOrganizationAccountId(role.getRoleId(), orgAccount.getOrganizationAccountId());
        if (orgAccountOwners.size() != 1) {
            log.error("More than one org account owner for org account with id [{}]", orgAccount.getOrganizationAccountId());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return orgAccountOwners
                .stream()
                .findFirst()
                .map(membershipRetrievalService::getExistingUserFromMembership)
                .orElseThrow(() -> {
                    log.error("The org account owner was not present for org account with id [{}]", orgAccount.getOrganizationAccountId());
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    private RoleEntity getExistingRole(String roleLevel) {
        return roleRepository.findFirstByRoleLevel(roleLevel)
                .<ResponseStatusException>orElseThrow(() -> {
                    log.error("Missing role with level [{}] in database", roleLevel);
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    private void validateUserHasAccessToOrgAccount(UserEntity authenticatedUserEntity, OrganizationAccountEntity orgAccountEntity, AccessOperation requestedAccessOperation) {
        boolean canAccess = organizationAccessService.canUserAccessOrganizationAccount(authenticatedUserEntity, orgAccountEntity, requestedAccessOperation);
        if (!canAccess) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

}
