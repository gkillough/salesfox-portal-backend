package com.usepipeline.portal.web.organization;

import com.usepipeline.portal.database.account.entity.MembershipEntity;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.account.repository.MembershipRepository;
import com.usepipeline.portal.database.account.repository.UserRepository;
import com.usepipeline.portal.database.organization.OrganizationEntity;
import com.usepipeline.portal.database.organization.OrganizationRepository;
import com.usepipeline.portal.database.organization.account.OrganizationAccountEntity;
import com.usepipeline.portal.database.organization.account.OrganizationAccountRepository;
import com.usepipeline.portal.web.organization.model.OrganizationAccountModel;
import com.usepipeline.portal.web.security.authentication.SecurityContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
public class OrganizationService {
    private UserRepository userRepository;
    private MembershipRepository membershipRepository;
    private OrganizationRepository organizationRepository;
    private OrganizationAccountRepository organizationAccountRepository;

    @Autowired
    public OrganizationService(UserRepository userRepository, MembershipRepository membershipRepository, OrganizationRepository organizationRepository, OrganizationAccountRepository organizationAccountRepository) {
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.organizationRepository = organizationRepository;
        this.organizationAccountRepository = organizationAccountRepository;
    }

    public OrganizationAccountModel getOrganizationAccount() {
        UserEntity loggedInUserEntity = SecurityContextUtils.retrieveUserAuthToken()
                .map(SecurityContextUtils::extractUserDetails)
                .map(UserDetails::getUsername)
                .flatMap(userRepository::findFirstByEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        // TODO abstract this repeated lookup logic into a service
        OrganizationAccountEntity organizationAccountEntity = membershipRepository.findFirstByUserId(loggedInUserEntity.getUserId())
                .map(MembershipEntity::getOrganizationAccountId)
                .flatMap(organizationAccountRepository::findById)
                .orElseThrow(() -> {
                    log.error("Missing Organization Account for user '{}' with id {}", loggedInUserEntity.getEmail(), loggedInUserEntity.getUserId());
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });

        OrganizationEntity organizationEntity = organizationRepository.findById(organizationAccountEntity.getOrganizationId())
                .orElseThrow(() -> {
                    log.error("Missing Organization for Organization Account '{}' with id {}", organizationAccountEntity.getOrganizationAccountName(), organizationAccountEntity.getOrganizationAccountId());
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });

        return new OrganizationAccountModel(organizationEntity.getOrganizationName(), organizationAccountEntity.getOrganizationAccountName());
    }

}
