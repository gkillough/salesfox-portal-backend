package com.usepipeline.portal.database.authentication.repository;

import com.usepipeline.portal.database.authentication.entity.MembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface MembershipRepository extends JpaRepository<MembershipEntity, Long> {
    Optional<MembershipEntity> findByUserId(Long userId);

    Optional<MembershipEntity> findByRoleId(Long roleId);

}
