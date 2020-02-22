package com.usepipeline.portal.database.authentication.repository;

import com.usepipeline.portal.database.authentication.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {
}
