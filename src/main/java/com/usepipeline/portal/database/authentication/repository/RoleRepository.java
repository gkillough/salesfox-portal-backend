package com.usepipeline.portal.database.authentication.repository;

import com.usepipeline.portal.database.authentication.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
}
