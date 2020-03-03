package com.usepipeline.portal.database.authentication.repository;

import com.usepipeline.portal.database.authentication.entity.LoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface LoginRepository extends JpaRepository<LoginEntity, Long> {
    Optional<LoginEntity> findFirstByUserId(Long userId);

}
