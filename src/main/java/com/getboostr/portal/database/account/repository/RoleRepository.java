package com.getboostr.portal.database.account.repository;

import com.getboostr.portal.database.account.entity.RoleEntity;
import com.getboostr.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findFirstByRoleLevel(String roleLevel);

    List<RoleEntity> findByRoleLevelStartingWith(String roleLevelPrefix);

    @Query("SELECT role" +
            " FROM RoleEntity role" +
            " WHERE role.roleLevel = '" + PortalAuthorityConstants.PORTAL_BASIC_USER + "'" +
            " OR role.roleLevel = '" + PortalAuthorityConstants.PORTAL_PREMIUM_USER + "'"
    )
    List<RoleEntity> getBasicAndPremiumRoles();

}
