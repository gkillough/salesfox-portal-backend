package com.usepipeline.portal.web.organization.common;

import com.usepipeline.portal.common.enumeration.AccessLevel;
import com.usepipeline.portal.database.account.entity.MembershipEntity;
import com.usepipeline.portal.database.account.entity.RoleEntity;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.account.repository.RoleRepository;
import com.usepipeline.portal.database.organization.account.OrganizationAccountEntity;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import com.usepipeline.portal.web.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrganizationAccessService {
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private RoleRepository roleRepository;

    @Autowired
    public OrganizationAccessService(HttpSafeUserMembershipRetrievalService membershipRetrievalService, RoleRepository roleRepository) {
        this.membershipRetrievalService = membershipRetrievalService;
        this.roleRepository = roleRepository;
    }

    public AccessLevel getAccessLevelForUserAndAccount(UserEntity userEntity, OrganizationAccountEntity organizationAccountEntity) {
        MembershipEntity membershipEntity = membershipRetrievalService.getMembershipEntity(userEntity);
        if (membershipEntity.getOrganizationAccountId().equals(organizationAccountEntity.getOrganizationAccountId())) {
            Optional<String> optionalOrgRoleLevel = roleRepository.findById(membershipEntity.getRoleId())
                    .map(RoleEntity::getRoleLevel)
                    .filter(level -> level.startsWith(PortalAuthorityConstants.ORGANIZATION_ROLE_PREFIX));
            if (optionalOrgRoleLevel.isPresent()) {
                switch (optionalOrgRoleLevel.get()) {
                    case PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER:
                        return AccessLevel.FULL;
                    case PortalAuthorityConstants.ORGANIZATION_ACCOUNT_MANAGER:
                        return AccessLevel.READ_WRITE_INSENSITIVE;
                    default:
                        return AccessLevel.READ_INSENSITIVE;
                }
            }
        }
        return AccessLevel.NONE;
    }

}
