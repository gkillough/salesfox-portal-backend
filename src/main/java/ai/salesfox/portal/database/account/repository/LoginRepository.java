package ai.salesfox.portal.database.account.repository;

import ai.salesfox.portal.database.account.entity.LoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface LoginRepository extends JpaRepository<LoginEntity, UUID> {

}
