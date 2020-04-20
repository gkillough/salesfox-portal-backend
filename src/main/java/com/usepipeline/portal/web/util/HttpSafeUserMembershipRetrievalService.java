package com.usepipeline.portal.web.util;

import com.usepipeline.portal.database.account.entity.MembershipEntity;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.account.repository.MembershipRepository;
import com.usepipeline.portal.database.account.repository.UserRepository;
import com.usepipeline.portal.database.organization.OrganizationEntity;
import com.usepipeline.portal.database.organization.OrganizationRepository;
import com.usepipeline.portal.database.organization.account.OrganizationAccountEntity;
import com.usepipeline.portal.database.organization.account.OrganizationAccountRepository;
import com.usepipeline.portal.web.security.authentication.SecurityContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Slf4j
@Component
public class HttpSafeUserMembershipRetrievalService {
    private UserRepository userRepository;
    private MembershipRepository membershipRepository;
    private OrganizationRepository organizationRepository;
    private OrganizationAccountRepository organizationAccountRepository;

    @Autowired
    public HttpSafeUserMembershipRetrievalService(UserRepository userRepository, MembershipRepository membershipRepository,
                                                  OrganizationRepository organizationRepository, OrganizationAccountRepository organizationAccountRepository) {
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.organizationRepository = organizationRepository;
        this.organizationAccountRepository = organizationAccountRepository;
    }

    public UserEntity getAuthenticatedUserEntity() {
        Optional<UsernamePasswordAuthenticationToken> authToken = SecurityContextUtils.retrieveUserAuthToken();
        if (authToken.isPresent()) {
            UserDetails userDetails = SecurityContextUtils.extractUserDetails(authToken.get());
            return getExistingUserByEmail(userDetails.getUsername());
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    public UserEntity getExistingUserByEmail(@NotNull String email) {
        return userRepository.findFirstByEmail(email)
                .orElseThrow(() -> {
                    log.error("Expected user with email [{}] to exist in the database", email);
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    public UserEntity getExistingUserFromMembership(@NotNull MembershipEntity membershipEntity) {
        return userRepository.findById(membershipEntity.getUserId())
                .orElseThrow(() -> {
                    log.error("Expected user with id [{}] and membership id [{}] to exist in the database", membershipEntity.getUserId(), membershipEntity.getMembershipId());
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    public MembershipEntity getMembershipEntity(@NotNull UserEntity userEntity) {
        return membershipRepository.findFirstByUserId(userEntity.getUserId())
                .orElseThrow(() -> {
                    log.error("Expected membership for user with id [{}] to exist in the database", userEntity.getUserId());
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    public OrganizationAccountEntity getOrganizationAccountEntity(@NotNull MembershipEntity membershipEntity) {
        return organizationAccountRepository.findById(membershipEntity.getOrganizationAccountId())
                .orElseThrow(() -> {
                    log.error("Expected organization account for user with id [{}], with membership id [{}] to exist in the database", membershipEntity.getUserId(), membershipEntity.getMembershipId());
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    public OrganizationEntity getOrganizationEntity(@NotNull OrganizationAccountEntity organizationAccountEntity) {
        return organizationRepository.findById(organizationAccountEntity.getOrganizationId())
                .orElseThrow(() -> {
                    log.error("Missing Organization for Organization Account '{}' with id {}", organizationAccountEntity.getOrganizationAccountName(), organizationAccountEntity.getOrganizationAccountId());
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

}
