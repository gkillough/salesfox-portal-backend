package com.usepipeline.portal.database.authentication.repository;

import com.usepipeline.portal.database.authentication.entity.PasswordResetTokenEntity;
import com.usepipeline.portal.database.authentication.key.PasswordResetTokenPK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, PasswordResetTokenPK> {
    void deleteByEmail(String email);
}
