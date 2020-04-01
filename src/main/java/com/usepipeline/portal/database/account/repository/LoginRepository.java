package com.usepipeline.portal.database.account.repository;

import com.usepipeline.portal.database.account.entity.LoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface LoginRepository extends JpaRepository<LoginEntity, Long> {
    Optional<LoginEntity> findFirstByUserId(Long userId);

}
