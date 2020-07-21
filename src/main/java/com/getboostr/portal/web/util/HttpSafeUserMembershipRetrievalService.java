package com.getboostr.portal.web.util;

import com.getboostr.portal.common.service.auth.AbstractMembershipRetrievalService;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.account.repository.MembershipRepository;
import com.getboostr.portal.database.account.repository.RoleRepository;
import com.getboostr.portal.database.account.repository.UserRepository;
import com.getboostr.portal.database.organization.OrganizationRepository;
import com.getboostr.portal.web.security.authentication.SecurityContextUtils;
import com.getboostr.portal.web.security.authorization.PortalAuthorityConstants;
import com.getboostr.portal.database.organization.account.OrganizationAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Slf4j
@Component
public class HttpSafeUserMembershipRetrievalService extends AbstractMembershipRetrievalService<ResponseStatusException> {
    @Autowired
    public HttpSafeUserMembershipRetrievalService(UserRepository userRepository, MembershipRepository membershipRepository,
                                                  RoleRepository roleRepository, OrganizationRepository organizationRepository, OrganizationAccountRepository organizationAccountRepository) {
        super(userRepository, membershipRepository, roleRepository, organizationRepository, organizationAccountRepository);
    }

    public UserDetails getAuthenticatedUserDetails() {
        Optional<UsernamePasswordAuthenticationToken> authToken = SecurityContextUtils.retrieveUserAuthToken();
        if (authToken.isPresent()) {
            return SecurityContextUtils.extractUserDetails(authToken.get());
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    public boolean isAuthenticatedUserPipelineAdmin() {
        return getAuthenticatedUserDetails()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(PortalAuthorityConstants.PORTAL_ADMIN::equals);
    }

    public boolean isAuthenticateUserBasicOrPremiumMember() {
        return getAuthenticatedUserDetails()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority ->
                        PortalAuthorityConstants.PORTAL_BASIC_USER.equals(authority)
                                || PortalAuthorityConstants.PORTAL_PREMIUM_USER.equals(authority)
                );
    }

    public UserEntity getAuthenticatedUserEntity() {
        UserDetails userDetails = getAuthenticatedUserDetails();
        return getExistingUserByEmail(userDetails.getUsername());
    }

    @Override
    public ResponseStatusException unexpectedErrorDuringRetrieval() {
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
