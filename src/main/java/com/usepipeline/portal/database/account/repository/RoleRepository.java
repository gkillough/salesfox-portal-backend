package com.usepipeline.portal.database.account.repository;

import com.usepipeline.portal.database.account.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findFirstByRoleLevel(String roleLevel);

}
