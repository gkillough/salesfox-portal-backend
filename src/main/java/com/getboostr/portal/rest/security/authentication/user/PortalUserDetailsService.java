package com.getboostr.portal.rest.security.authentication.user;

import com.getboostr.portal.common.time.PortalDateTimeUtils;
import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.RoleEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.account.repository.LoginRepository;
import com.getboostr.portal.database.account.repository.MembershipRepository;
import com.getboostr.portal.database.account.repository.RoleRepository;
import com.getboostr.portal.database.account.repository.UserRepository;
import com.getboostr.portal.database.account.entity.LoginEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;

@Component
@Slf4j
public class PortalUserDetailsService implements UserDetailsService {
    private MembershipRepository membershipRepository;
    private LoginRepository loginRepository;
    private UserRepository userRepository;
    private RoleRepository roleRepository;

    @Autowired
    public PortalUserDetailsService(MembershipRepository membershipRepository, LoginRepository loginRepository, UserRepository userRepository, RoleRepository roleRepository) {
        this.membershipRepository = membershipRepository;
        this.loginRepository = loginRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // FIXME replace with reasonable joins
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

        boolean userLocked = isUserLocked(userLogin);
        if (userLocked) {
            log.debug("User: [{}]. Time since last locked: [{}].", username, userLogin.getLastLocked());
        }
        return new PortalUserDetails(userRoles, user.getEmail(), userLogin.getPasswordHash(), userLocked, user.getIsActive());
    }

    public boolean isUserLocked(LoginEntity userLogin) {
        OffsetDateTime lastLockedTime = userLogin.getLastLocked();
        if (lastLockedTime != null) {
            Duration timeSinceLocked = Duration.between(lastLockedTime, PortalDateTimeUtils.getCurrentDateTimeUTC());
            if (timeSinceLocked.compareTo(PortalUserLoginAttemptService.DURATION_UNTIL_ACCOUNT_UNLOCKED) < 0) {
                // If the time since the account was locked is strictly less than the time needed to unlock the account, then the account is still locked.
                return true;
            }
        }
        return false;
    }

}
