package com.getboostr.portal.database.account.repository;

import com.getboostr.portal.database.account.entity.LoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public interface LoginRepository extends JpaRepository<LoginEntity, UUID> {
    Optional<LoginEntity> findFirstByUserId(UUID userId);

}
