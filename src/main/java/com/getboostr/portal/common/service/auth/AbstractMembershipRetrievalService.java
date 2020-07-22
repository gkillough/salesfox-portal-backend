package com.getboostr.portal.common.service.auth;

import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.RoleEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.account.repository.UserRepository;
import com.getboostr.portal.database.organization.OrganizationEntity;
import com.getboostr.portal.database.organization.OrganizationRepository;
import com.getboostr.portal.database.organization.account.OrganizationAccountEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotNull;

@Slf4j
public abstract class AbstractMembershipRetrievalService<E extends Throwable> {
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    @Autowired
    public AbstractMembershipRetrievalService(UserRepository userRepository, OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
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

    @Deprecated
    public MembershipEntity getMembershipEntity(@NotNull UserEntity userEntity) throws E {
        return userEntity.getMembershipEntity();
    }

    @Deprecated
    public RoleEntity getRoleEntity(@NotNull MembershipEntity membershipEntity) throws E {
        return membershipEntity.getRoleEntity();
    }

    @Deprecated
    public OrganizationAccountEntity getOrganizationAccountEntity(@NotNull MembershipEntity membershipEntity) throws E {
        return membershipEntity.getOrganizationAccountEntity();
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
