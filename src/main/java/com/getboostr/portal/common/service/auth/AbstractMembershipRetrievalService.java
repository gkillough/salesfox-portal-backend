package com.getboostr.portal.common.service.auth;

import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.RoleEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.account.repository.MembershipRepository;
import com.getboostr.portal.database.account.repository.RoleRepository;
import com.getboostr.portal.database.account.repository.UserRepository;
import com.getboostr.portal.database.organization.OrganizationEntity;
import com.getboostr.portal.database.organization.OrganizationRepository;
import com.getboostr.portal.database.organization.account.OrganizationAccountEntity;
import com.getboostr.portal.database.organization.account.OrganizationAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotNull;

@Slf4j
public abstract class AbstractMembershipRetrievalService<E extends Throwable> {
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationAccountRepository organizationAccountRepository;

    @Autowired
    public AbstractMembershipRetrievalService(UserRepository userRepository, MembershipRepository membershipRepository,
                                              RoleRepository roleRepository, OrganizationRepository organizationRepository, OrganizationAccountRepository organizationAccountRepository) {
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
        this.organizationAccountRepository = organizationAccountRepository;
    }

    public UserEntity getExistingUserByEmail(@NotNull String email) throws E {
        return userRepository.findFirstByEmail(email)
                .orElseThrow(() -> {
                    log.error("Expected user with email [{}] to exist in the database", email);
                    return unexpectedErrorDuringRetrieval();
                });
    }

    public UserEntity getExistingUserFromMembership(@NotNull MembershipEntity membershipEntity) throws E {
        return userRepository.findById(membershipEntity.getUserId())
                .orElseThrow(() -> {
                    log.error("Expected user with id [{}] to exist in the database", membershipEntity.getUserId());
                    return unexpectedErrorDuringRetrieval();
                });
    }

    public MembershipEntity getMembershipEntity(@NotNull UserEntity userEntity) throws E {
        return membershipRepository.findById(userEntity.getUserId())
                .orElseThrow(() -> {
                    log.error("Expected membership for user with id [{}] to exist in the database", userEntity.getUserId());
                    return unexpectedErrorDuringRetrieval();
                });
    }

    public RoleEntity getRoleEntity(@NotNull MembershipEntity membershipEntity) throws E {
        return roleRepository.findById(membershipEntity.getRoleId())
                .orElseThrow(() -> {
                    log.error("Expected role with id [{}] for user id [{}] to exist in the database", membershipEntity.getRoleId(), membershipEntity.getUserId());
                    return unexpectedErrorDuringRetrieval();
                });
    }

    public OrganizationAccountEntity getOrganizationAccountEntity(@NotNull MembershipEntity membershipEntity) throws E {
        return organizationAccountRepository.findById(membershipEntity.getOrganizationAccountId())
                .orElseThrow(() -> {
                    log.error("Expected organization account for user with id [{}] to exist in the database", membershipEntity.getUserId());
                    return unexpectedErrorDuringRetrieval();
                });
    }

    public OrganizationEntity getOrganizationEntity(@NotNull OrganizationAccountEntity organizationAccountEntity) throws E {
        return organizationRepository.findById(organizationAccountEntity.getOrganizationId())
                .orElseThrow(() -> {
                    log.error("Missing Organization for Organization Account [{}] with id [{}]", organizationAccountEntity.getOrganizationAccountName(), organizationAccountEntity.getOrganizationAccountId());
                    return unexpectedErrorDuringRetrieval();
                });
    }

    public abstract E unexpectedErrorDuringRetrieval();

}
