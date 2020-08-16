package com.getboostr.portal.rest.security.authentication.user;

import com.getboostr.portal.common.time.PortalDateTimeUtils;
import com.getboostr.portal.database.account.entity.LoginEntity;
import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.RoleEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.account.repository.RoleRepository;
import com.getboostr.portal.database.account.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PortalUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public PortalUserDetailsService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findFirstByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user"));
        LoginEntity userLogin = user.getLoginEntity();
        MembershipEntity membership = user.getMembershipEntity();

        List<String> userRoles = new ArrayList<>();
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
            Duration timeSinceLocked = Duration.between(lastLockedTime, PortalDateTimeUtils.getCurrentDateTime());
            if (timeSinceLocked.compareTo(PortalUserLoginAttemptService.DURATION_UNTIL_ACCOUNT_UNLOCKED) < 0) {
                // If the time since the account was locked is strictly less than the time needed to unlock the account, then the account is still locked.
                return true;
            }
        }
        return false;
    }

}
