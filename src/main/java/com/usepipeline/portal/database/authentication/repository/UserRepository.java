package com.usepipeline.portal.database.authentication.repository;

import com.usepipeline.portal.database.authentication.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
