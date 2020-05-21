package com.usepipeline.portal.database.account.repository;

import com.usepipeline.portal.database.account.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findFirstByEmail(String email);
}
