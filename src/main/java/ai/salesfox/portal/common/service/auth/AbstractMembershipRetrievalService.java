package ai.salesfox.portal.common.service.auth;

import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.RoleEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.account.repository.UserRepository;
import ai.salesfox.portal.database.organization.OrganizationEntity;
import ai.salesfox.portal.database.organization.OrganizationRepository;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class AbstractMembershipRetrievalService<E extends Throwable> {
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    @Autowired
    public AbstractMembershipRetrievalService(UserRepository userRepository, OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    public UserEntity getExistingUserByEmail(String email) throws E {
        return userRepository.findFirstByEmail(email)
                .orElseThrow(() -> {
                    log.error("Expected user with email [{}] to exist in the database", email);
                    return unexpectedErrorDuringRetrieval();
                });
    }

    public UserEntity getExistingUserFromMembership(MembershipEntity membershipEntity) throws E {
        return userRepository.findById(membershipEntity.getUserId())
                .orElseThrow(() -> {
                    log.error("Expected user with id [{}] to exist in the database", membershipEntity.getUserId());
                    return unexpectedErrorDuringRetrieval();
                });
    }

    @Deprecated
    public MembershipEntity getMembershipEntity(UserEntity userEntity) throws E {
        return userEntity.getMembershipEntity();
    }

    @Deprecated
    public RoleEntity getRoleEntity(MembershipEntity membershipEntity) throws E {
        return membershipEntity.getRoleEntity();
    }

    @Deprecated
    public OrganizationAccountEntity getOrganizationAccountEntity(MembershipEntity membershipEntity) throws E {
        return membershipEntity.getOrganizationAccountEntity();
    }

    public OrganizationEntity getOrganizationEntity(OrganizationAccountEntity organizationAccountEntity) throws E {
        return organizationRepository.findById(organizationAccountEntity.getOrganizationId())
                .orElseThrow(() -> {
                    log.error("Missing Organization for Organization Account [{}] with id [{}]", organizationAccountEntity.getOrganizationAccountName(), organizationAccountEntity.getOrganizationAccountId());
                    return unexpectedErrorDuringRetrieval();
                });
    }

    public abstract E unexpectedErrorDuringRetrieval();

}
