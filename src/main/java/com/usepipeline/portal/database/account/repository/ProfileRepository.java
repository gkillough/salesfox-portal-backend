package com.usepipeline.portal.database.account.repository;

import com.usepipeline.portal.database.account.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {
    Optional<ProfileEntity> findFirstByUserId(Long userId);

}
