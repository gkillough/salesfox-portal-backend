package ai.salesfox.portal.database.account.repository;

import ai.salesfox.portal.database.account.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface ProfileRepository extends JpaRepository<ProfileEntity, UUID> {
}
