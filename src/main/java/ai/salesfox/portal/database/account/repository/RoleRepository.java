package ai.salesfox.portal.database.account.repository;

import ai.salesfox.portal.database.account.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findFirstByRoleLevel(String roleLevel);

    List<RoleEntity> findByRoleLevelStartingWith(String roleLevelPrefix);

}
