package com.usepipeline.portal.web.user.common;

import com.usepipeline.portal.database.authentication.entity.MembershipEntity;
import com.usepipeline.portal.database.authentication.entity.UserEntity;
import com.usepipeline.portal.database.authentication.repository.MembershipRepository;
import com.usepipeline.portal.database.authentication.repository.RoleRepository;
import com.usepipeline.portal.database.authentication.repository.UserRepository;
import com.usepipeline.portal.web.security.authentication.SecurityContextUtils;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import com.usepipeline.portal.web.user.role.UserRoleModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

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

    public boolean canCurrentUserAccessDataForUser(Long userId) {
        Optional<UsernamePasswordAuthenticationToken> optionalAuthToken = SecurityContextUtils.retrieveUserAuthToken();
        if (optionalAuthToken.isPresent()) {
            UserDetails userDetails = SecurityContextUtils.extractUserDetails(optionalAuthToken.get());
            String email = userDetails.getUsername();
            Optional<UserEntity> optionalUser = userRepository.findFirstByEmail(email);
            if (optionalUser.isPresent()) {
                UserEntity userEntity = optionalUser.get();
                Long entityUserId = userEntity.getUserId();
                // TODO in the future, we may want to also check if the logged in user manages the user with this userId
                if (entityUserId == userId) {
                    // If the logged in user is the requested user, grant access.
                    return true;
                } else {
                    // If the logged in user is a pipeline admin, grant access.
                    return userDetails.getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority)
                            .anyMatch(PortalAuthorityConstants.PIPELINE_ADMIN::equals);
                }
            }

        }
        return false;
    }

    public UserRoleModel findRoleByUserId(Long userId) {
        return membershipRepository.findFirstByUserId(userId)
                .map(MembershipEntity::getRoleId)
                .flatMap(roleRepository::findById)
                .map(roleEntity -> new UserRoleModel(roleEntity.getRoleLevel(), roleEntity.getDescription()))
                .orElse(UserRoleModel.ANONYMOUS_ROLE);
    }

}
