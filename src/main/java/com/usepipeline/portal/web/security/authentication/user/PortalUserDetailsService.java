package com.usepipeline.portal.web.security.authentication.user;

import com.usepipeline.portal.database.authentication.entity.LoginEntity;
import com.usepipeline.portal.database.authentication.entity.MembershipEntity;
import com.usepipeline.portal.database.authentication.entity.RoleEntity;
import com.usepipeline.portal.database.authentication.entity.UserEntity;
import com.usepipeline.portal.database.authentication.repository.LoginRepository;
import com.usepipeline.portal.database.authentication.repository.MembershipRepository;
import com.usepipeline.portal.database.authentication.repository.RoleRepository;
import com.usepipeline.portal.database.authentication.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
@Slf4j
public class PortalUserDetailsService implements UserDetailsService {
    private MembershipRepository membershipRepository;
    private LoginRepository loginRepository;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PortalUserLoginAttemptService portalUserLoginAttemptService;

    @Autowired
    public PortalUserDetailsService(MembershipRepository membershipRepository, LoginRepository loginRepository, UserRepository userRepository, RoleRepository roleRepository,
                                    PortalUserLoginAttemptService portalUserLoginAttemptService) {
        this.membershipRepository = membershipRepository;
        this.loginRepository = loginRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.portalUserLoginAttemptService = portalUserLoginAttemptService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // FIXME replace with reasonable joins
        // FIXME replace with better exceptions
        UserEntity user = userRepository.findFirstByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user"));
        LoginEntity userLogin = loginRepository.findFirstByUserId(user.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("No login"));
        MembershipEntity membership = membershipRepository.findFirstByUserId(userLogin.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("No membership"));

        ArrayList<String> userRoles = new ArrayList<>();
        roleRepository.findById(membership.getRoleId())
                .map(RoleEntity::getRoleLevel)
                .ifPresent(userRoles::add);


        boolean userLocked = isUserLocked(username, userLogin);
        return new PortalUserDetails(userRoles, user.getEmail(), userLogin.getPasswordHash(), userLocked, membership.getIsActive());
    }

    private boolean isUserLocked(String username, LoginEntity userLogin) {
        LocalDateTime lastLockedTime = userLogin.getLastLocked();
        if (lastLockedTime != null) {
            log.debug("User: [{}]. Time since last locked: [{}].", username, lastLockedTime);
            Duration timeSinceLocked = Duration.between(lastLockedTime, LocalDateTime.now());
            if (timeSinceLocked.compareTo(PortalUserLoginAttemptService.DURATION_UNTIL_ACCOUNT_UNLOCKED) < 0) {
                // If the time since the account was locked is strictly less than the time needed to unlock the account,
                // then the account is still locked.
                return true;
            }
        }
        return false;
    }

}
