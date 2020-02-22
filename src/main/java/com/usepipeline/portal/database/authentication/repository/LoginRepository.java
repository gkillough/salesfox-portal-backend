package com.usepipeline.portal.database.authentication.repository;

import com.usepipeline.portal.database.authentication.entity.LoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface LoginRepository extends JpaRepository<LoginEntity, Long> {
    @Query(nativeQuery = true,
            value = "SELECT * FROM pipeline.logins l " +
                    "JOIN pipline.users u ON l.user_id = u.user_id" +
                    "WHERE email = '$1';")
    Optional<LoginEntity> findByEmail(String email);
}
