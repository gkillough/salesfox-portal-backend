package ai.salesfox.portal.database.account.repository;

import ai.salesfox.portal.database.account.entity.MembershipEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public interface MembershipRepository extends JpaRepository<MembershipEntity, UUID> {
    Page<MembershipEntity> findByOrganizationAccountId(UUID organizationAccountId, Pageable pageable);

    List<MembershipEntity> findByRoleIdAndOrganizationAccountId(UUID roleId, UUID organizationAccountId);

}
