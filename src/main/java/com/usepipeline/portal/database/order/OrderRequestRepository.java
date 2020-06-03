package com.usepipeline.portal.database.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public interface OrderRequestRepository extends JpaRepository<OrderRequestEntity, UUID> {
    List<OrderRequestEntity> findByOrganizationAccountId(UUID organizationAccountId);

    List<OrderRequestEntity> findByUserId(UUID userId);

    List<OrderRequestEntity> findByRequestingUserId(UUID requestingUserId);

}