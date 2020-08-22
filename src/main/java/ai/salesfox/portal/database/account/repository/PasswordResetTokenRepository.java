package ai.salesfox.portal.database.account.repository;

import ai.salesfox.portal.database.account.entity.PasswordResetTokenEntity;
import ai.salesfox.portal.database.account.key.PasswordResetTokenPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, PasswordResetTokenPK> {
    void deleteByEmail(String email);

}
