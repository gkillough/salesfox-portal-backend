package com.usepipeline.portal.database.account.repository;

import com.usepipeline.portal.database.account.entity.MembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public interface MembershipRepository extends JpaRepository<MembershipEntity, Long> {
    Optional<MembershipEntity> findFirstByUserId(Long userId);

    List<MembershipEntity> findByRoleId(Long roleId);

    List<MembershipEntity> findByRoleIdAndOrganizationAccountId(Long roleId, Long organizationAccountId);

}
