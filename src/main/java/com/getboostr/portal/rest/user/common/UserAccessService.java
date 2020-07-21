package com.getboostr.portal.rest.user.common;

import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.account.repository.MembershipRepository;
import com.getboostr.portal.database.account.repository.RoleRepository;
import com.getboostr.portal.database.account.repository.UserRepository;
import com.getboostr.portal.rest.security.authentication.SecurityContextUtils;
import com.getboostr.portal.rest.security.authorization.PortalAuthorityConstants;
import com.getboostr.portal.rest.user.role.model.UserRoleModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserAccessService {
    private UserRepository userRepository;
    private MembershipRepository membershipRepository;
    private RoleRepository roleRepository;

    @Autowired
    public UserAccessService(UserRepository userRepository, MembershipRepository membershipRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.roleRepository = roleRepository;
    }

    public boolean canCurrentUserAccessDataForUser(UUID userId) {
        Optional<UsernamePasswordAuthenticationToken> optionalAuthToken = SecurityContextUtils.retrieveUserAuthToken();
        if (optionalAuthToken.isPresent()) {
            UserDetails userDetails = SecurityContextUtils.extractUserDetails(optionalAuthToken.get());
            String email = userDetails.getUsername();
            Optional<UserEntity> optionalUser = userRepository.findFirstByEmail(email);
            if (optionalUser.isPresent()) {
                UserEntity userEntity = optionalUser.get();
                UUID entityUserId = userEntity.getUserId();
                // TODO in the future, we may want to also check if the logged in user manages the user with this userId
                if (entityUserId.equals(userId)) {
                    // If the logged in user is the requested user, grant access.
                    return true;
                } else {
                    // If the logged in user is a pipeline admin, grant access.
                    return userDetails.getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority)
                            .anyMatch(PortalAuthorityConstants.PORTAL_ADMIN::equals);
                }
            }
        }
        return false;
    }

    public UserRoleModel findRoleByUserId(UUID userId) {
        return membershipRepository.findFirstByUserId(userId)
                .map(MembershipEntity::getRoleId)
                .flatMap(roleRepository::findById)
                .map(roleEntity -> new UserRoleModel(roleEntity.getRoleLevel(), roleEntity.getDescription()))
                .orElse(UserRoleModel.ANONYMOUS_ROLE);
    }

}
